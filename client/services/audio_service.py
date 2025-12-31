import os, tempfile, sounddevice as sd, soundfile as sf

class AudioService:
    def __init__(self):
        self.fs = 16000
        self.duration = 5
        # 临时录音文件保存到 data/audio/
        base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        self.audio_dir = os.path.join(base_dir, "data", "audio")
        os.makedirs(self.audio_dir, exist_ok=True)

    def record(self, duration=None):
        if duration is None:
            duration = self.duration
        print("开始录音...")
        recording = sd.rec(int(duration * self.fs), samplerate=self.fs, channels=1)
        sd.wait()
        print("录音完成")

        temp_file = tempfile.NamedTemporaryFile(dir=self.audio_dir, delete=False, suffix=".wav")
        sf.write(temp_file.name, recording, self.fs)
        return temp_file.name
