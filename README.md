# Sentinel

A scalable, full-stack **Content Moderation Service** that detects offensive **text** (Java algorithms) and **image/GIF content** (Python + OpenCV), processes messages via **Kafka**, and provides a beautiful **ReactJS dashboard** for human moderators.


# Proposed Model
```aiignore
+------------------+        +------------------+
|   Frontend (UI)  | <----> | Spring Boot API  |
+------------------+        +------------------+
                                   |
                             +-----------+
                             |  Kafka    |
                             +-----------+
                             |           |
                +------------------+   +------------------------+
                | Text Moderation  |   | Image Moderation (Py) |
                | Java Core Logic  |   | FastAPI + OpenCV      |
                +------------------+   +------------------------+
                                   |
                          +------------------+
                          |   PostgreSQL     |
                          +------------------+

```

---

## Tech Stack

| Layer           | Tech                       |
|----------------|----------------------------|
| **Backend API** | Spring Boot, Java, Kafka   |
| **Text Filter** | Custom Java algorithms |
| **Image Filter**| Python 3 + OpenCV + FastAPI |
| **Messaging**   | Apache Kafka               |
| **Database**    | PostgreSQL                 |
| **Frontend UI** | ReactJS + TailwindCSS      |
| **Deployment**  | Docker, Kubernetes, GCP (Cloud Run / GKE) |

---
# Additional Notes
This project is under development and will undergo changes.