from PySide6.QtWidgets import QWidget, QGridLayout
from PySide6.QtCore import Signal
from .home_card import HomeCard


class HomePage(QWidget):
    openFeature = Signal(str)

    def __init__(self):
        super().__init__()
        layout = QGridLayout(self)
        layout.setSpacing(30)
        layout.setContentsMargins(30, 30, 30, 30)

        self.add_card(layout, 0, 0, "📁 文件管理", "file_manager")

    def add_card(self, layout, row, col, title, key):
        card = HomeCard(title, key)
        card.clicked.connect(self.openFeature.emit)
        layout.addWidget(card, row, col)
