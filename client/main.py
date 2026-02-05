import configparser
import os
import subprocess
import sys
import traceback
from datetime import datetime
from pathlib import Path

import cv2
from PySide6.QtCore import QSize, QThreadPool, QRunnable, QObject, QTimer
from PySide6.QtCore import Qt, Signal
from PySide6.QtGui import QFontMetrics
from PySide6.QtGui import QPixmap, QImage, QPalette
from PySide6.QtWidgets import (
    QApplication, QWidget, QHBoxLayout, QLineEdit, QPushButton,
    QComboBox, QTreeWidget, QTreeWidgetItem, QScrollArea,
    QGridLayout, QStyle
)
from PySide6.QtWidgets import QFrame, QLabel, QVBoxLayout, QToolTip

# -------------------- 配置 --------------------
CONFIG_PATH = "config.ini"

def load_config():
    config = configparser.ConfigParser()
    if not os.path.exists(CONFIG_PATH):
        config["DEFAULT"] = {"HomeDir": os.path.expanduser("~")}
        with open(CONFIG_PATH, "w", encoding="utf-8") as f:
            config.write(f)
    else:
        config.read(CONFIG_PATH, encoding="utf-8")
    return config

def save_config(home_dir):
    config = configparser.ConfigParser()
    config["DEFAULT"] = {"HomeDir": home_dir}
    with open(CONFIG_PATH, "w", encoding="utf-8") as f:
        config.write(f)

# -------------------- 全局变量 --------------------
IMAGE_EXTS = [".png", ".jpg", ".jpeg", ".bmp", ".gif"]
VIDEO_EXTS = [".mp4", ".avi", ".mov", ".mkv"]
VIDEO_THUMB_CACHE = {}

# -------------------- 异步缩略图 --------------------
class ThumbnailWorkerSignals(QObject):
    finished = Signal(QWidget, QPixmap)

class ThumbnailWorker(QRunnable):
    def __init__(self, card_widget, file_path, icon_size, is_video=False):
        super().__init__()
        self.card_widget = card_widget
        self.file_path = str(Path(file_path).resolve())
        self.icon_size = icon_size
        self.is_video = is_video
        self.signals = ThumbnailWorkerSignals()

    def run(self):
        try:
            pixmap = None
            if self.is_video:
                if self.file_path in VIDEO_THUMB_CACHE:
                    pixmap = VIDEO_THUMB_CACHE[self.file_path]
                else:
                    pixmap = FileBrowser.get_video_thumbnail_static(self.file_path, self.icon_size)
                    if pixmap:
                        VIDEO_THUMB_CACHE[self.file_path] = pixmap
            else:
                pixmap = QPixmap(self.file_path).scaled(
                    self.icon_size, Qt.KeepAspectRatio, Qt.SmoothTransformation
                )
            if pixmap and not pixmap.isNull():
                self.signals.finished.emit(self.card_widget, pixmap)
        except Exception:
            traceback.print_exc()
            self.signals.finished.emit(self.card_widget, QPixmap(self.icon_size))

# -------------------- 卡片控件 --------------------

class FileCard(QFrame):
    doubleClicked = Signal(str)
    clicked = Signal(QWidget)
    CARD_WIDTH = 140
    CARD_HEIGHT = 140

    def __init__(self, name, path, placeholder_pixmap, base_color, highlight_color, modified_time="", size=""):
        super().__init__()
        self.setFixedSize(self.CARD_WIDTH, self.CARD_HEIGHT)
        self.file_path = path
        self.modified_time = modified_time
        self.size = size
        self.is_selected = False
        self.base_color = base_color
        self.highlight_color = highlight_color

        # QFrame 基本样式
        self.setFrameShape(QFrame.StyledPanel)
        self.setLineWidth(1)
        self.setStyleSheet("QFrame { border-radius: 4px; }")  # 圆角

        layout = QVBoxLayout(self)
        layout.setContentsMargins(2,2,2,2)
        layout.setSpacing(2)
        layout.addStretch()  # 顶部空白，让内容靠底部

        # 图标
        self.icon_label = QLabel()
        self.icon_label.setPixmap(placeholder_pixmap)
        self.icon_label.setAlignment(Qt.AlignHCenter | Qt.AlignBottom)
        layout.addWidget(self.icon_label)

        # 文件名
        self.text_label = QLabel()
        self.text_label.setAlignment(Qt.AlignHCenter | Qt.AlignTop)
        self.text_label.setWordWrap(True)
        self.set_file_name(name)
        layout.addWidget(self.text_label)

        self.update_style()

    def set_file_name(self, name):
        """设置文件名显示两行，多余省略"""
        fm = QFontMetrics(self.text_label.font())
        line_height = fm.lineSpacing()
        self.text_label.setFixedHeight(line_height)

        # 使用 elidedText 处理多余文字
        # 注意 QLabel 不自带多行省略，所以简单处理：把文字按宽度截断
        elided = fm.elidedText(name, Qt.ElideRight, self.CARD_WIDTH - 4)
        # 显示两行时，直接显示同样文本即可，如果太长会显示...
        self.text_label.setText(elided)

    def update_style(self):
        if self.is_selected:
            bg_color = "#0a64d8"
            text_color = "#ffffff"

        else:
            bg_color = self.base_color.name()
            text_color = "#000000"

        self.setStyleSheet(f"""
            QFrame {{
                background-color: {bg_color};
                border-radius: 4px;
            }}
        """)
        self.text_label.setStyleSheet(f"color: {text_color};")

    def set_selected(self, selected: bool):
        self.is_selected = selected
        self.update_style()

    def mousePressEvent(self, event):
        if event.button() == Qt.LeftButton:
            self.clicked.emit(self)

    def mouseDoubleClickEvent(self, event):
        if event.button() == Qt.LeftButton:
            self.doubleClicked.emit(self.file_path)

    def enterEvent(self, event):
        global_pos = event.globalPosition().toPoint()
        QToolTip.showText(global_pos, f"文件名称: {self.text_label.text()}\n修改时间: {self.modified_time}\n大小: {self.size}", self)



# -------------------- 主浏览器 --------------------
class FileBrowser(QWidget):
    ICON_SIZE = QSize(120, 120)
    MIN_COLUMNS = 3
    MAX_COLUMNS = 10

    def __init__(self):
        super().__init__()
        config = load_config()
        self.current_dir = config["DEFAULT"].get("HomeDir", os.path.expanduser("~"))
        self.thread_pool = QThreadPool.globalInstance()
        self.thread_pool.setMaxThreadCount(8)

        # 视图数据
        self.sorted_files = []
        self.card_widgets = []
        self.loaded_files = 0
        self.total_files = 0
        self._lazy_index = 0
        self.selected_card = None

        # 背景颜色
        dummy_tree = QTreeWidget()
        self.base_color = dummy_tree.palette().color(QPalette.Base)
        self.highlight_color = dummy_tree.palette().color(QPalette.Highlight)

        # 占位图
        self.placeholder = QPixmap(self.ICON_SIZE)

        self.init_ui()

    def init_ui(self):
        layout = QVBoxLayout(self)
        layout.setContentsMargins(0,0,0,0)

        # 顶部栏
        top = QHBoxLayout()
        self.back_btn = QPushButton("⬅ 上一级")
        self.back_btn.clicked.connect(self.go_up)
        top.addWidget(self.back_btn)

        self.path_input = QLineEdit()
        self.path_input.returnPressed.connect(self.load_directory)
        top.addWidget(self.path_input)

        refresh_btn = QPushButton("刷新")
        refresh_btn.clicked.connect(self.load_directory)
        top.addWidget(refresh_btn)

        self.view_combo = QComboBox()
        self.view_combo.addItems(["列表","卡片"])
        self.view_combo.currentTextChanged.connect(self.change_view_mode)
        top.addWidget(self.view_combo)

        home_btn = QPushButton("设置首页目录")
        home_btn.clicked.connect(self.set_home_directory_dialog)
        top.addWidget(home_btn)

        layout.addLayout(top)

        # 列表视图
        self.tree = QTreeWidget()
        self.tree.setColumnCount(5)
        self.tree.setHeaderLabels(["名称","类型","大小","创建时间","修改时间"])
        self.tree.setColumnWidth(0,300)
        self.tree.itemDoubleClicked.connect(self.open_tree_item)
        self.tree.itemClicked.connect(self.tree_item_click)
        self.tree.hide()
        layout.addWidget(self.tree)

        # 卡片视图
        self.scroll_area = QScrollArea()
        self.scroll_area.setWidgetResizable(True)
        self.card_container = QWidget()
        self.grid_layout = QGridLayout(self.card_container)
        self.grid_layout.setSpacing(8)
        self.grid_layout.setAlignment(Qt.AlignTop | Qt.AlignLeft)
        self.grid_layout.setContentsMargins(10,10,10,10)
        self.scroll_area.setWidget(self.card_container)
        self.scroll_area.hide()
        layout.addWidget(self.scroll_area)

        # 进度
        self.progress = QLabel("加载中...")
        self.progress.setAlignment(Qt.AlignRight | Qt.AlignBottom)
        layout.addWidget(self.progress)

        # 初始化默认视图
        self.path_input.setText(self.current_dir)
        self.view_combo.setCurrentText("列表")
        self.view_mode = self.view_combo.currentText()
        self.change_view_mode(self.view_mode)

    # ---------------- 模式切换 ----------------
    def change_view_mode(self, mode):
        self.view_mode = mode
        self.tree.setVisible(mode=="列表")
        self.scroll_area.setVisible(mode=="卡片")
        self.load_directory()

    # ---------------- 加载目录 ----------------
    def load_directory(self):
        path = self.path_input.text().strip()
        if not path or not os.path.isdir(path):
            self.progress.setText("无效目录")
            return
        self.current_dir = path
        self.path_input.setText(path)

        files = [f for f in os.listdir(path) if not f.startswith('.') and not f.startswith('_')]
        paths = [os.path.join(path, f) for f in files]
        stats = [os.stat(p) for p in paths]

        dirs = sorted([(f, p, s) for f, p, s in zip(files, paths, stats) if os.path.isdir(p)],
                      key=lambda x: x[2].st_mtime, reverse=True)
        normals = sorted([(f, p, s) for f, p, s in zip(files, paths, stats) if not os.path.isdir(p)],
                         key=lambda x: x[2].st_mtime, reverse=True)
        sorted_items = dirs + normals
        self.sorted_files = [item[0] for item in sorted_items]
        self.file_stats = {item[0]: item[2] for item in sorted_items}

        self.total_files = len(self.sorted_files)
        self.loaded_files = 0
        self.progress.setText(f"加载中: {self.loaded_files}/{self.total_files}")
        self._lazy_index = 0
        self.selected_card = None

        if self.view_mode == "列表":
            self.load_tree()
        else:
            self.clear_cards()
            QTimer.singleShot(50, lambda: self.lazy_load_batch(initial=True))

    # ---------------- 列表视图 ----------------
    def load_tree(self):
        self.tree.clear()
        for f in self.sorted_files:
            p = os.path.join(self.current_dir,f)
            stat = self.file_stats[f]
            size = f"{stat.st_size/1024:.1f} KB" if not os.path.isdir(p) else ""
            ctime = datetime.fromtimestamp(stat.st_ctime).strftime("%Y-%m-%d %H:%M")
            mtime = datetime.fromtimestamp(stat.st_mtime).strftime("%Y-%m-%d %H:%M")
            item = QTreeWidgetItem([f,"文件夹" if os.path.isdir(p) else "文件",size,ctime,mtime])
            icon = self.style().standardIcon(QStyle.SP_DirIcon if os.path.isdir(p) else QStyle.SP_FileIcon)
            item.setIcon(0,icon)
            self.tree.addTopLevelItem(item)
        self.progress.setText("加载完成")

    def tree_item_click(self,item,column):
        # 单选点击同步卡片选中
        if self.view_mode=="卡片":
            for card in self.card_widgets:
                if card.file_path==os.path.join(self.current_dir,item.text(0)):
                    self.select_card(card)
                    break

    # ---------------- 卡片视图 ----------------
    def clear_cards(self):
        for i in reversed(range(self.grid_layout.count())):
            widget = self.grid_layout.itemAt(i).widget()
            if widget:
                widget.setParent(None)
        self.card_widgets.clear()

    def lazy_load_batch(self, initial=False):
        cols = self.columns()
        screen_rows = max(1,self.scroll_area.height()//(FileCard.CARD_HEIGHT+8))
        batch_size = 30 if not initial else (screen_rows*cols*2)

        count = 0
        while self._lazy_index < len(self.sorted_files) and count<batch_size:
            name = self.sorted_files[self._lazy_index]
            path = os.path.join(self.current_dir,name)
            stat = self.file_stats[name]
            mtime = datetime.fromtimestamp(stat.st_mtime).strftime("%Y-%m-%d %H:%M")
            size = f"{stat.st_size/1024:.1f} KB" if not os.path.isdir(path) else "文件夹"
            card = FileCard(name,path,self.placeholder,self.base_color,self.highlight_color,mtime,size)

            suffix = Path(path).suffix.lower()
            if suffix in IMAGE_EXTS:
                worker = ThumbnailWorker(card,path,self.ICON_SIZE,False)
                worker.signals.finished.connect(self.update_card_icon)
                self.thread_pool.start(worker)
            elif suffix in VIDEO_EXTS:
                worker = ThumbnailWorker(card,path,self.ICON_SIZE,True)
                worker.signals.finished.connect(self.update_card_icon)
                self.thread_pool.start(worker)
            else:
                pix = self.style().standardIcon(QStyle.SP_DirIcon if os.path.isdir(path) else QStyle.SP_FileIcon).pixmap(self.ICON_SIZE)
                card.icon_label.setPixmap(pix)

            card.doubleClicked.connect(self.open_path)
            card.clicked.connect(self.select_card)

            row = len(self.card_widgets)//cols
            col = len(self.card_widgets)%cols
            self.grid_layout.addWidget(card,row,col)
            self.card_widgets.append(card)

            self._lazy_index+=1
            count+=1

        if self._lazy_index<len(self.sorted_files):
            QTimer.singleShot(50,lambda:self.lazy_load_batch())
        else:
            self.progress.setText("加载完成")

    def select_card(self, card):
        if self.selected_card:
            self.selected_card.set_selected(False)
        card.set_selected(True)
        self.selected_card = card

    def columns(self):
        if self.width()<300: return 1
        card_w = FileCard.CARD_WIDTH+12
        cols = self.width()//card_w
        return max(self.MIN_COLUMNS,min(self.MAX_COLUMNS,cols))

    # ---------------- 更新图标 ----------------
    def update_card_icon(self, card_widget: QWidget, pixmap: QPixmap):
        card_widget.icon_label.setPixmap(pixmap)

    # ---------------- 打开路径 ----------------
    def open_tree_item(self,item):
        self.open_path(os.path.join(self.current_dir,item.text(0)))

    def open_path(self,path):
        try:
            if os.path.isdir(path):
                self.path_input.setText(path)
                self.load_directory()
            else:
                if sys.platform.startswith("darwin"):
                    subprocess.run(["open",path])
                elif os.name=="nt":
                    os.startfile(path)
                else:
                    subprocess.run(["xdg-open",path])
        except Exception:
            traceback.print_exc()
            self.progress.setText("打开失败")

    # ---------------- 上一级 ----------------
    def go_up(self):
        parent = os.path.dirname(self.current_dir)
        if parent and parent!=self.current_dir:
            self.path_input.setText(parent)
            self.load_directory()

    # ---------------- 视频缩略图 ----------------
    @staticmethod
    def get_video_thumbnail_static(path,size):
        try:
            cap = cv2.VideoCapture(str(Path(path).resolve()))
            if not cap.isOpened(): return None
            total = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
            cap.set(cv2.CAP_PROP_POS_FRAMES,max(0,int(total*0.2)))
            ok,frame = cap.read()
            cap.release()
            if ok:
                frame = cv2.cvtColor(frame,cv2.COLOR_BGR2RGB)
                h,w,ch = frame.shape
                img = QImage(frame.data,w,h,ch*w,QImage.Format_RGB888)
                return QPixmap.fromImage(img).scaled(size,Qt.KeepAspectRatio,Qt.SmoothTransformation)
        except Exception:
            traceback.print_exc()
        return None

    def resizeEvent(self,e):
        super().resizeEvent(e)
        if self.view_mode=="卡片" and self.card_widgets:
            cols = self.columns()
            for idx,card in enumerate(self.card_widgets):
                row = idx//cols
                col = idx%cols
                self.grid_layout.addWidget(card,row,col)
            self.grid_layout.invalidate()

    # ---------------- 设置首页目录 ----------------
    def set_home_directory_dialog(self):
        path = self.path_input.text().strip()
        if os.path.isdir(path):
            save_config(path)
            self.progress.setText(f"首页目录已设置为: {path}")

# -------------------- main --------------------
if __name__=="__main__":
    app = QApplication(sys.argv)
    w = QWidget()
    l = QVBoxLayout(w)
    browser = FileBrowser()
    l.addWidget(browser)
    w.resize(1200,720)
    w.show()
    sys.exit(app.exec())
