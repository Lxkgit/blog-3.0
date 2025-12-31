from PySide6.QtWidgets import QWidget, QVBoxLayout, QPushButton, QTextEdit
from services.speech_service import SpeechService
from services.llm_service import LLMService

class VoiceChatPage(QWidget):
    """
    语音聊天页面
    - 支持麦克风录音
    - 支持系统声音录音
    - 识别文字后调用 ChatGPT 回复
    """

    def __init__(self, main_window):
        """
        :param parent: 父窗口（可选）
        """
        super().__init__()
        self.main_window = main_window

        # 初始化语音服务和 ChatGPT 服务
        self.speech_service = SpeechService()
        self.llm_service = LLMService()

        self.init_ui()



    def init_ui(self):
        """初始化界面"""
        layout = QVBoxLayout()

        # 麦克风录音按钮
        self.home_btn = QPushButton("返回首页")
        self.home_btn.clicked.connect(self.main_window.go_home)
        layout.addWidget(self.home_btn)

        # 聊天显示框
        self.chat_box = QTextEdit()
        self.chat_box.setReadOnly(True)
        layout.addWidget(self.chat_box)

        # 麦克风录音按钮
        self.mic_btn = QPushButton("录制麦克风")
        self.mic_btn.clicked.connect(self.start_mic_recording)
        layout.addWidget(self.mic_btn)

        # 系统声音录音按钮
        self.system_btn = QPushButton("录制系统声音")
        self.system_btn.clicked.connect(self.start_system_recording)
        layout.addWidget(self.system_btn)

        self.setLayout(layout)

    def start_mic_recording(self):
        """录制麦克风音频并识别"""
        try:
            audio_file = self.speech_service.record_mic()
            self.chat_box.append(f"麦克风录音完成，文件：{audio_file}")

            text = self.speech_service.recognize(audio_file)
            self.chat_box.append(f"识别文字：{text}")

            reply = self.llm_service.ask(text)
            self.chat_box.append(f"ChatGPT 回复：{reply}")
        except Exception as e:
            self.chat_box.append(f"麦克风录音或识别失败：{e}")

    def start_system_recording(self):
        """录制系统声音并识别"""
        try:
            from services.system_audio_service import SystemAudioRecorder

            # 选择 Windows 环回设备 Stereo Mix
            recorder = SystemAudioRecorder(device="立体声混音 (Realtek High Definition Audio), Windows WASAPI")
            audio_file = recorder.record()
            self.chat_box.append(f"系统录音完成，文件：{audio_file}")

            text = self.speech_service.recognize(audio_file)
            self.chat_box.append(f"识别文字：{text}")

            reply = self.llm_service.ask(text)
            self.chat_box.append(f"ChatGPT 回复：{reply}")
        except Exception as e:
            self.chat_box.append(f"系统录音或识别失败：{e}")
