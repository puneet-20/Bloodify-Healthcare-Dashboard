```markdown
# 🩸 Bloodify: Biometric Healthcare Dashboard

> 🚀 Production-grade healthcare system for biometric blood group detection and intelligent donor matching.

---

## 🎯 Why This Project Stands Out

- 🧠 Simulates a real-world healthcare workflow (Detection → Eligibility → Matching → Reporting)  
- ⚙️ Built with scalable backend architecture using Spring Boot & JPA  
- 🚨 Implements Emergency Priority Logic (real-world triage system)  
- 📊 Interactive analytics dashboard using Chart.js  
- 📄 Generates secure PDF reports with QR verification  
- 🔌 Demonstrates biometric API integration capability  

---

## 🧩 Problem Statement

In emergencies, finding the right blood donor quickly is critical.  
Traditional systems lack intelligent prioritization, fast matching, and verified reporting.

**Bloodify solves this using biometric input, smart matching, and emergency prioritization.**

---

## 🏗️ System Design

```

User Input (Biometric/File)
↓
Prediction Engine (Mock AI + Rules)
↓
Eligibility Engine (Health + Time Constraints)
↓
Matching Engine (City + Compatibility)
↓
Emergency Override (O- Priority)
↓
PDF Report + QR Code

````

---
## ML Service — Real-Time Fingerprint Blood Group Prediction

Integrates live fingerprint capture (SecuGen Hamster Pro 20) with a trained CNN model that predicts blood group from the scanned fingerprint.

**Flow:** SecuGen Scanner → SgiBioSrv (local client) → Browser → Spring Boot backend → FastAPI ML service (`/predict`) → Dashboard.

### Prerequisites
- Python 3.12+
- SecuGen SgiBioSrv client app running locally (provides `https://localhost:8000/SGIFPCapture`)
- SecuGen Hamster Pro 20 connected via USB

### Setup

## ✨ Core Features

### 🧬 Biometric Detection
- SecuGen WebAPI integration  
- File upload fallback  
- AI-based prediction (mocked) with confidence score  
- Probability visualization using Chart.js  

---

### 🩺 Eligibility Engine
- Validates donor using:
  - 56-day donation rule  
  - Health conditions  
- Prevents invalid entries  

---

### 🔎 Donor Matching
- Filters based on:
  - Blood group compatibility  
  - City  
- Optimized queries using JPA/Hibernate  

---

### 🚨 Emergency Mode
- Prioritizes O- donors  
- Overrides normal matching logic  
- Highlights critical donors  

---

### 📄 PDF Generation
- Medical certificate generation  
- Includes QR code for verification  
- Built using iText7  

---

### 🎨 Frontend
- Responsive UI (HTML, CSS, JS)  
- Dark/Light mode  
- Interactive charts  
- Clean dashboard design  

---

## 🛠️ Tech Stack

- Backend: Java 17, Spring Boot 3  
- Database: H2 (JPA + Hibernate)  
- Frontend: HTML, CSS, JavaScript  
- Charts: Chart.js  
- PDF: iText7  
- Build Tool: Maven  

---

## 🚀 Run the Project

```bash
git clone https://github.com/your-username/bloodify.git
cd bloodify
mvn clean compile spring-boot:run
````

Open: https://bloodify-healthcare-dashboard.onrender.com

---

## 📊 Key Highlights

* Multi-layer architecture (Controller → Service → Repository)
* Rule-based decision system
* Real-time filtering and matching
* API + hardware integration
* End-to-end workflow implementation

---

## 📈 Learning Outcomes

* Backend system design using Spring Boot
* Real-world business logic implementation
* API integration and fallback handling
* Data visualization
* Clean project structuring

---

## 🔮 Future Scope

* Integrate real ML model
* Add authentication (JWT)
* Deploy on cloud (AWS/Docker)
* Mobile app version
* Real-time location tracking

---

## 👨‍💻 Developer

Puneet Raut
Final Year Engineering Student

---

## ⭐ Note

This project demonstrates strong backend development, system design thinking, and real-world problem solving.

```
```
