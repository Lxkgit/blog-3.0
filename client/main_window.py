from PySide6.QtWidgets import QMainWindow, QTabWidget
from home.home_page import HomePage
from features import FEATURE_REGISTRY


class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("客户端主界面")
        self.resize(1200, 720)

        self.tabs = QTabWidget()
        self.tabs.setTabsClosable(True)
        self.tabs.tabCloseRequested.connect(self.close_tab)
        self.setCentralWidget(self.tabs)

        self.opened_tabs = {}

        self.home = HomePage()
        self.home.openFeature.connect(self.open_feature)

        self.tabs.addTab(self.home, "🏠 首页")
        self.tabs.tabBar().setTabButton(0, QTabWidget.RightSide, None)

    def open_feature(self, key: str):
        if key in self.opened_tabs:
            self.tabs.setCurrentWidget(self.opened_tabs[key])
            return

        widget_cls = FEATURE_REGISTRY.get(key)
        if not widget_cls:
            return

        widget = widget_cls()
        index = self.tabs.addTab(widget, widget.TITLE)
        self.tabs.setCurrentIndex(index)
        self.opened_tabs[key] = widget

    def close_tab(self, index: int):
        if index == 0:
            return

        widget = self.tabs.widget(index)
        self.tabs.removeTab(index)
        widget.deleteLater()

        for k, v in list(self.opened_tabs.items()):
            if v == widget:
                del self.opened_tabs[k]
