from PySide6.QtWidgets import QFrame, QLabel, QVBoxLayout
from PySide6.QtCore import Qt, Signal


class HomeCard(QFrame):
    clicked = Signal(str)

    def __init__(self, title: str, key: str):
        super().__init__()
        self.key = key
        self.setFixedSize(160, 120)
        self.setCursor(Qt.PointingHandCursor)

        self.setStyleSheet("""
            QFrame {
                background-color: #f5f5f5;
                border-radius: 8px;
            }
            QFrame:hover {
                background-color: #e6f0ff;
            }
        """)

        layout = QVBoxLayout(self)
        layout.setAlignment(Qt.AlignCenter)

        label = QLabel(title)
        label.setAlignment(Qt.AlignCenter)
        label.setStyleSheet("font-size: 16px;")
        layout.addWidget(label)

    def mousePressEvent(self, event):
        if event.button() == Qt.LeftButton:
            self.clicked.emit(self.key)
