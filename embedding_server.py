"""
轻量级 Embedding HTTP 服务 — 替代 Ollama，全部运行在 E 盘
端口: 11435 (避免与可能存在的 Ollama 冲突)
模型: all-MiniLM-L6-v2 (~80MB, 384维)
"""
import json
import sys
import os
from http.server import HTTPServer, BaseHTTPRequestHandler

# 模型缓存到 E 盘 + 使用国内 HuggingFace 镜像
os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'
os.environ['HF_HOME'] = 'E:/huggingface'
os.environ['TRANSFORMERS_CACHE'] = 'E:/huggingface/models'
os.environ['SENTENCE_TRANSFORMERS_HOME'] = 'E:/huggingface/sbert'

print("[embedding_server] Loading model (first time downloads ~80MB to E:/huggingface)...")
from sentence_transformers import SentenceTransformer
model = SentenceTransformer('all-MiniLM-L6-v2')
print("[embedding_server] Model loaded. Ready on port 11435.")


class EmbeddingHandler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path == '/api/embeddings' or self.path == '/embeddings':
            try:
                length = int(self.headers.get('Content-Length', 0))
                body = json.loads(self.rfile.read(length))
                inp = body.get('input', body.get('text', 'Hello World'))
                if isinstance(inp, str):
                    inp = [inp]
                embeddings = model.encode(inp, normalize_embeddings=True)
                result = {
                    "object": "list",
                    "data": [{"object": "embedding", "index": i, "embedding": emb.tolist()}
                             for i, emb in enumerate(embeddings)],
                    "model": "all-MiniLM-L6-v2",
                    "usage": {"prompt_tokens": sum(len(s.split()) for s in inp), "total_tokens": sum(len(s.split()) for s in inp)}
                }
                resp = json.dumps(result).encode()
                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.send_header('Content-Length', len(resp))
                self.end_headers()
                self.wfile.write(resp)
            except Exception as e:
                err = json.dumps({"error": str(e)}).encode()
                self.send_response(500)
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                self.wfile.write(err)
        else:
            self.send_response(404)
            self.end_headers()

    def do_GET(self):
        if self.path == '/health':
            self.send_response(200)
            self.end_headers()
            self.wfile.write(b'OK')
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        print(f"[embedding_server] {args[0]}")


if __name__ == '__main__':
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 11435
    server = HTTPServer(('127.0.0.1', port), EmbeddingHandler)
    print(f"[embedding_server] Listening on http://127.0.0.1:{port}")
    server.serve_forever()
