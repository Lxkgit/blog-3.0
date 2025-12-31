import sounddevice as sd
import soundfile as sf
import tempfile

class SystemAudioRecorder:
    """
    系统扬声器录音服务

    使用方法：
        recorder = SystemAudioRecorder(device="Stereo Mix")
        audio_file = recorder.record()
        print("保存音频到：", audio_file)
    """

    def __init__(self, fs=16000, duration=5, device=None):
        """
        初始化录音服务
        :param fs: 采样率，默认 16000 Hz
        :param duration: 录音时长，默认 5 秒
        :param device: 录音设备名称或 ID，None 使用系统默认
        """
        self.fs = fs
        self.duration = duration
        self.device = device  # None 默认系统录音设备

    def record(self):
        """
        开始录制系统声音
        :return: 临时 wav 文件路径
        """
        print("开始录制系统声音...")
        # 录制 stereo（双声道），方便捕获系统声音
        recording = sd.rec(int(self.duration * self.fs), samplerate=self.fs, channels=2, device=self.device)
        sd.wait()
        print("录制完成")

        # 保存到临时文件
        temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".wav")
        sf.write(temp_file.name, recording, self.fs)
        return temp_file.name