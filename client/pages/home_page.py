from PySide6.QtWidgets import QWidget, QVBoxLayout, QPushButton, QLabel

class HomePage(QWidget):
    def __init__(self, main_window):
        super().__init__()
        self.main_window = main_window

        layout = QVBoxLayout()

        title = QLabel("欢迎使用博客客户端")
        title.setStyleSheet("font-size: 22px; font-weight: bold;")

        voice_btn = QPushButton("🎤 语音识别 & ChatGPT")
        voice_btn.setFixedHeight(60)
        voice_btn.clicked.connect(self.main_window.go_voice_chat)

        layout.addSpacing(40)
        layout.addWidget(title)
        layout.addSpacing(30)
        layout.addWidget(voice_btn)
        layout.addStretch()

        self.setLayout(layout)
