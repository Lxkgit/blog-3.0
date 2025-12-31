from PySide6.QtWidgets import QMainWindow, QStackedWidget
from pages.home_page import HomePage
from pages.voice_chat_page import VoiceChatPage

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("博客客户端")
        self.resize(900, 600)

        self.stack = QStackedWidget()

        self.home_page = HomePage(self)
        self.voice_page = VoiceChatPage(self)

        self.stack.addWidget(self.home_page)   # index 0
        self.stack.addWidget(self.voice_page)  # index 1

        self.setCentralWidget(self.stack)

    def go_home(self):
        self.stack.setCurrentIndex(0)

    def go_voice_chat(self):
        self.stack.setCurrentIndex(1)
