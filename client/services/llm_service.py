class LLMService:
    def ask(self, text: str) -> str:
        # 这里先返回示例文字，后续可替换为 OpenAI API
        return f"收到问题：{text}（这里是 ChatGPT 回复）"
