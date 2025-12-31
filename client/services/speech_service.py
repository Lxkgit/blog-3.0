import os
import wave
from vosk import Model, KaldiRecognizer

class SpeechService:
    def __init__(self):
        base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        model_path = os.path.join(base_dir, "models", "vosk_model")
        self.model = Model(model_path)

    def recognize(self, audio_file_path):
        wf = wave.open(audio_file_path, "rb")
        rec = KaldiRecognizer(self.model, wf.getframerate())

        result_text = ""
        while True:
            data = wf.readframes(4000)
            if len(data) == 0:
                break
            if rec.AcceptWaveform(data):
                result_text += rec.Result()
        result_text += rec.FinalResult()
        return result_text