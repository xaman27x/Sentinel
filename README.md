# Sentinel 
A Content Moderation Service that detects offensive *text* (**Trie Based Metric System**) with a dashboard for human moderators.

---

## Overview

**Sentinel** is a modular and extensible content moderation platform built for real-time flagging of offensive content. Designed to integrate easily into web and mobile applications, Sentinel provides a microservice-based architecture for detecting harmful text, while enabling human oversight through a user-friendly dashboard.

---

## ⚙️ Moderation Philosophy

Sentinel's moderation engine is a **deterministic rule-based system** optimized for speed, security, and adaptability. The flow of text analysis follows a multi-layered pipeline:

### Text Moderation Pipeline:

1. **Preprocessing**
   - Lowercasing, unicode normalization, and whitespace trimming.
   - Removal of unnecessary punctuation and control characters.

2. **Normalization**
   - Converts characters like leetspeak (`h3ll0` → `hello`) and visual obfuscations.
   - Resolves homoglyphs (e.g., Cyrillic “а” vs Latin “a”).

3. **Obfuscation Logic**
   - Detects character shifts, insertion patterns, and token splits.
   - Flags suspicious sequences using regex heuristics.

4. **Soundex Detection**
   - Applies phonetic hashing (Soundex) to match sound-alike words.
   - Used to detect homophones and spelling variants.

5. **Fuzzy Matching & Edit Distance**
   - Applies **Levenshtein distance** with a definitive threshold.
   - Measures closeness of suspicious words to banned terms in the dictionary.

6. **Encrypted Trie Matching**
   - At runtime, an encrypted binary `.dat` wordlist is loaded into memory.
   - A **Trie (Prefix Tree)** is built for fast lookup of offensive terms.
   - Trie is `@Autowired` into the moderation service on boot.
   - Words pass through all rules and are scored against the Trie with confidence metrics.

---

## Key Features

- **Real-Time Text Moderation**
  - Trie-based filtering with encrypted wordlists.
  - Levenshtein + Soundex + fuzzy heuristics.
  - Caching for sub-ms inference (<1ms/token).

- **Secure & Scalable APIs** 
  - Spring Boot APIs secured with JWT + API Keys.
  - Admin-configurable keys via React dashboard.

- **Moderator Dashboard** 
  - Built with ReactJS + TailwindCSS.
  - Role-based moderation and review workflows.

---

## 🛠 Tech Stack

| Layer                | Tech                                   |
|----------------------|----------------------------------------|
| Backend API          | Spring Boot, Java                      |
| Text Filtering       | Custom Algorithms (DSA, Trie, Soundex) |
| Database             | PostgreSQL (NeonDB)                    |
| Frontend             | ReactJS, TailwindCSS                   |
| Deployment (Backend) | Docker, GCP (Cloud Run)                |
| Deployment (Frontend)| AWS Amplify                            |

---

## Development Status

This project is actively under development. Upcoming features include:
- Kafka-based consumer-producer pipeline
- Algorithm based visual moderation

---
# Contributing to Sentinel

Thank you for your interest in contributing to **Sentinel**! Contributions are welcomed from developers to help improve the content moderation platform.

---

## Ways to Contribute

- **Report Bugs**  
  Found something that doesn’t work? [Open an issue](https://github.com/xaman27x/sentinel/issues) describing the problem, expected behavior, and reproduction steps.

- **Feature Requests**  
  Have an idea for a new feature or improvement? We’d love to hear about it.

- **Code Contributions**  
  Improve existing logic, fix bugs, or add new capabilities — whether backend (Java/Python), frontend (React), or infrastructure (Docker).

- **Documentation Improvements**  
  Help us improve clarity, add missing details, or fix typos.



## Setup & Installation

```bash
# Clone repository
git clone https://github.com/your-org/sentinel.git
cd sentinel

# Backend (Java - Spring Boot)
cd backend
./mvnw spring-boot:run
```
