package com.sigmadevs.aiintegration.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Service
public class ProjectService {

    public static void main(String[] args) {
        generateProject("""
                {
                   "package.json": "{\\n  \\"name\\": \\"react-vite-login-register\\",\\n  \\"private\\": true,\\n  \\"version\\": \\"0.0.0\\",\\n  \\"type\\": \\"module\\",\\n  \\"scripts\\": {\\n    \\"dev\\": \\"vite\\",\\n    \\"build\\": \\"vite build\\",\\n    \\"preview\\": \\"vite preview\\"\\n  },\\n  \\"dependencies\\": {\\n    \\"react\\": \\"^18.2.0\\",\\n    \\"react-dom\\": \\"^18.2.0\\"\\n  },\\n  \\"devDependencies\\": {\\n    \\"@vitejs/plugin-react\\": \\"^4.0.0\\",\\n    \\"vite\\": \\"^4.0.0\\"\\n  }\\n}",
                   "vite.config.js": "import { defineConfig } from 'vite';\\nimport react from '@vitejs/plugin-react';\\n\\nexport default defineConfig({\\n  plugins: [react()]\\n});",
                   "index.html": "<!DOCTYPE html>\\n<html lang=\\"en\\">\\n  <head>\\n    <meta charset=\\"UTF-8\\" />\\n    <meta name=\\"viewport\\" content=\\"width=device-width, initial-scale=1.0\\" />\\n    <title>React Vite App</title>\\n  </head>\\n  <body>\\n    <div id=\\"root\\"></div>\\n    <script type=\\"module\\" src=\\"/src/main.jsx\\"></script>\\n  </body>\\n</html>",
                   "src/main.jsx": "import React from 'react';\\nimport ReactDOM from 'react-dom/client';\\nimport App from './App.jsx';\\n\\nReactDOM.createRoot(document.getElementById('root')).render(\\n  <React.StrictMode>\\n    <App />\\n  </React.StrictMode>\\n);",
                   "src/App.jsx": "import React from 'react';\\nimport Login from './Login.jsx';\\nimport Register from './Register.jsx';\\n\\nexport default function App() {\\n  return (\\n    <div>\\n      <h1>Welcome to My App</h1>\\n      <Login />\\n      <Register />\\n    </div>\\n  );\\n}",
                   "src/Login.jsx": "import React, { useState } from 'react';\\n\\nexport default function Login() {\\n  const [email, setEmail] = useState('');\\n  const [password, setPassword] = useState('');\\n\\n  const handleSubmit = (e) => {\\n    e.preventDefault();\\n    console.log('Login Submitted:', { email, password });\\n  };\\n\\n  return (\\n    <div>\\n      <h2>Login</h2>\\n      <form onSubmit={handleSubmit}>\\n        <div>\\n          <label>Email:</label>\\n          <input\\n            type='email'\\n            value={email}\\n            onChange={(e) => setEmail(e.target.value)}\\n            required\\n          />\\n        </div>\\n        <div>\\n          <label>Password:</label>\\n          <input\\n            type='password'\\n            value={password}\\n            onChange={(e) => setPassword(e.target.value)}\\n            required\\n          />\\n        </div>\\n        <button type='submit'>Login</button>\\n      </form>\\n    </div>\\n  );\\n}",
                   "src/Register.jsx": "import React, { useState } from 'react';\\n\\nexport default function Register() {\\n  const [email, setEmail] = useState('');\\n  const [password, setPassword] = useState('');\\n  const [confirmPassword, setConfirmPassword] = useState('');\\n\\n  const handleSubmit = (e) => {\\n    e.preventDefault();\\n    console.log('Registration Submitted:', { email, password, confirmPassword });\\n  };\\n\\n  return (\\n    <div>\\n      <h2>Register</h2>\\n      <form onSubmit={handleSubmit}>\\n        <div>\\n          <label>Email:</label>\\n          <input\\n            type='email'\\n            value={email}\\n            onChange={(e) => setEmail(e.target.value)}\\n            required\\n          />\\n        </div>\\n        <div>\\n          <label>Password:</label>\\n          <input\\n            type='password'\\n            value={password}\\n            onChange={(e) => setPassword(e.target.value)}\\n            required\\n          />\\n        </div>\\n        <div>\\n          <label>Confirm Password:</label>\\n          <input\\n            type='password'\\n            value={confirmPassword}\\n            onChange={(e) => setConfirmPassword(e.target.value)}\\n            required\\n          />\\n        </div>\\n        <button type='submit'>Register</button>\\n      </form>\\n    </div>\\n  );\\n}"
                 }
                """);
    }

    public static void generateProject(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);

//            File projectsDir = new File(new ClassPathResource("projects").getFile().getAbsolutePath());
            File projectsDir = new File("src/main/resources/projects");
            if (!projectsDir.exists()) {
                projectsDir.mkdirs();
            }

            String dirName = "project" + UUID.randomUUID().toString();
            File projectDir = new File(projectsDir, dirName);
            if (!projectDir.exists()) {
                projectDir.mkdirs();
            }

            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String filePath = entry.getKey();
                String content = entry.getValue().asText();

                File file = new File(projectDir, filePath);
                file.getParentFile().mkdirs(); // Створюємо всі необхідні папки

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(content);
                }
            }

            System.out.println("success: " + projectDir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
