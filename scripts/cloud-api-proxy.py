#!/usr/bin/env python3
"""
Simple API Proxy - Forwards requests to local backend server
Run: python3 api-proxy.py
"""

import http.server
import socketserver
import urllib.request
import urllib.error
import json
import sys
from urllib.parse import urlparse, parse_qs

PORT = 8888
LOCAL_API = "http://192.168.2.32:8083"

class ProxyHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        self.proxy_request()
    
    def do_POST(self):
        self.proxy_request()
    
    def do_PUT(self):
        self.proxy_request()
    
    def do_DELETE(self):
        self.proxy_request()
    
    def proxy_request(self):
        # Get the path and remove leading slash
        path = self.path
        if path == '/':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps({"status": "ok", "service": "api-proxy"}).encode())
            return
        
        # Forward to local backend
        target_url = f"{LOCAL_API}{path}"
        
        # Read request body if POST/PUT
        content_length = int(self.headers.get('Content-Length', 0))
        body = self.rfile.read(content_length) if content_length > 0 else None
        
        # Get headers
        headers = {}
        for key, value in self.headers.items():
            if key.lower() not in ['host', 'connection']:
                headers[key] = value
        
        try:
            # Make the request
            req = urllib.request.Request(target_url, data=body, headers=headers, method=self.command)
            with urllib.request.urlopen(req, timeout=30) as response:
                self.send_response(response.status)
                
                # Copy headers
                for key, value in response.getheaders():
                    if key.lower() not in ['transfer-encoding', 'connection']:
                        self.send_header(key, value)
                self.send_header('Access-Control-Allow-Origin', '*')
                self.end_headers()
                
                # Copy response body
                self.wfile.write(response.read())
                
        except urllib.error.HTTPError as e:
            self.send_response(e.code)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            error_body = e.read() if e.fp else b'{"error": "proxy error"}'
            self.wfile.write(error_body)
            
        except Exception as e:
            self.send_response(502)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(json.dumps({"error": str(e)}).encode())
    
    def log_message(self, format, *args):
        print(f"[{self.address_string()}] {format % args}")

if __name__ == '__main__':
    print(f"Starting API Proxy on port {PORT}")
    print(f"Forwarding to: {LOCAL_API}")
    
    with socketserver.TCPServer(("", PORT), ProxyHandler) as httpd:
        httpd.serve_forever()
