AI-Powered Urine and Stool Diagnostic Application
Overview
This project is an AI-assisted mobile healthcare application designed to support the preliminary analysis of urine and stool samples using machine learning.
The application allows users to capture or upload sample images, analyze them using an on-device TensorFlow Lite Convolutional Neural Network (CNN) model, view preliminary results, access a gastrointestinal health chatbot, save analysis history, and communicate important health alerts to connected doctors.
The system is designed as a preventive health-support tool. It aims to help users identify possible abnormalities early and determine when professional medical attention may be necessary.
The application also includes a dedicated doctor interface, allowing doctors to monitor connected patients, review urgent alerts, add private notes, acknowledge alerts, resolve alerts, and view patients' previous analysis history.
Disclaimer: This application is intended for educational and preliminary health-support purposes only. It does not replace professional medical diagnosis, laboratory testing, or consultation with a qualified healthcare professional.
________________________________________
Main Objectives
The main objectives of the project are to:
•	Provide AI-assisted analysis of urine and stool samples.
•	Offer users quick preliminary health information.
•	Support early identification of possible gastrointestinal and urinary abnormalities.
•	Reduce delays associated with waiting for initial health assessment.
•	Help users determine when further medical diagnosis may be necessary.
•	Allow doctors to receive and review urgent patient alerts.
•	Provide users with access to a gastrointestinal and urinary health chatbot.
•	Maintain a history of previous analysis results.
________________________________________
Key Features
Patient Features
Urine Analysis
Users can capture an image using the phone camera or select an existing image from the gallery.
The image is processed directly on the Android device using a trained TensorFlow Lite machine-learning model.
The application then displays the predicted classification or analysis result.
________________________________________
Stool Analysis
Users can also capture or upload stool sample images for AI-assisted analysis.
The integrated CNN model analyzes the image and returns a preliminary classification.
________________________________________
On-Device AI Analysis
The machine-learning model was originally developed and trained using Python and TensorFlow.
After training, the model was converted into TensorFlow Lite (.tflite) format and integrated directly into the Android application.
This allows image classification to take place locally on the user's device without requiring a separate machine-learning server.
________________________________________
Camera and Gallery Support
Users can provide sample images through:
•	Device camera
•	Device gallery
The selected image is displayed before analysis.
________________________________________
Analysis Results
After an image is processed, the application displays the predicted result to the user.
The interface can also provide information about possible next steps based on the result.
The system should not be interpreted as providing a confirmed medical diagnosis.
________________________________________
Analysis History
Users can save and view previous urine and stool analysis results.
The history contains information such as:
•	Analysis type
•	Classification or result
•	Date
•	Time
________________________________________
GI Health Assistant
The project includes a gastrointestinal and urinary health chatbot.
The chatbot allows users to ask questions related to:
•	Stool health
•	Urine health
•	Gastrointestinal symptoms
•	General digestive health information
•	Understanding health-related concerns
The chatbot is built using:
•	LangChain
•	Streamlit
•	FAISS
•	OpenAI embeddings
•	PDF-based knowledge source
The chatbot is intended to provide general health information and should not replace professional medical advice.
________________________________________
Doctor Features
The application also contains a dedicated interface for doctors.
Doctors use the same Android application but log in using a Firebase account configured with:
role: doctor
After successful login, doctors are automatically directed to the Doctor Dashboard.
________________________________________
Doctor Dashboard
The Doctor Dashboard provides access to important patient information and alerts.
It displays the doctor's unique application ID, which can be used by patients to connect to the doctor.
________________________________________
Assigned Patients
Doctors can view a list of patients currently assigned to their account.
Each patient can be opened to view additional information.
________________________________________
Live Urgent Alerts
While the Doctor Dashboard is open, urgent patient alerts are displayed in real time.
These alerts may contain information such as:
•	Patient email
•	Urgent message
•	Analysis result
•	Date and time
•	Alert status
________________________________________
Alert Details
Doctors can open individual alerts to review the associated information.
An alert may contain:
•	Patient email
•	Analysis result
•	Urgent message
•	Attached image, where Firebase Storage support is available
•	Date and time
•	Current alert status
________________________________________
Private Doctor Notes
Doctors can add private notes to individual patient alerts.
These notes are intended for the doctor's own reference.
________________________________________
Alert Status Management
Doctors can update urgent alerts using statuses such as:
•	New
•	Acknowledged
•	Resolved
This helps doctors manage patient alerts more effectively.
________________________________________
Patient Details
Doctors can open a patient's profile and review:
•	Previous urgent alerts
•	Previous stool analysis results
•	Previous urine analysis results
•	Analysis date and time
•	Classification results
________________________________________
Features Not Yet Active
The following features are planned but are not currently fully implemented:
•	Background push notifications when the doctor application is closed
•	Firebase Cloud Messaging notifications
•	Doctor access to historical patient sample images
•	Patient invitation and approval workflow
•	QR-code-based patient and doctor connection
•	Alert filtering by New, Acknowledged, and Resolved
•	Doctor-to-patient messaging
•	Full cloud synchronization of sample images
________________________________________
Technology Stack
Android Application
•	Android Studio
•	Java
•	XML
•	Android Material Components
________________________________________
Machine Learning
•	Python
•	TensorFlow
•	Convolutional Neural Network
•	TensorFlow Lite
•	Google Colab
The CNN model is converted from TensorFlow format to TensorFlow Lite format and runs directly inside the Android application.
________________________________________
Chatbot
•	Python
•	LangChain
•	Streamlit
•	FAISS
•	OpenAI Embeddings
•	PyPDF2
•	python-dotenv
________________________________________
Cloud and Doctor Features
•	Firebase Authentication
•	Firebase role-based accounts
•	Firebase Realtime Database or Firestore
•	Firebase Storage where image support is enabled
Firebase is used for features such as:
•	Doctor authentication
•	User roles
•	Doctor identification
•	Patient assignments
•	Urgent alerts
•	Alert status
•	Doctor notes
•	Patient history synchronization
________________________________________
System Architecture
The system uses a combination of on-device artificial intelligence and cloud-supported doctor monitoring.
                    +----------------------+
                    |      PATIENT APP     |
                    |   Android / Java     |
                    +----------+-----------+
                               |
             +-----------------+-----------------+
             |                                   |
             v                                   v
+------------------------+            +------------------------+
| Camera / Gallery Input |            | GI Health Assistant    |
+-----------+------------+            | LangChain + Streamlit  |
            |                         +------------------------+
            v
+------------------------+
| TensorFlow Lite CNN    |
| On-Device Analysis     |
+-----------+------------+
            |
            v
+------------------------+
| Analysis Result        |
| History / Guidance     |
+-----------+------------+
            |
            v
+------------------------+
| Firebase               |
| Alerts / Patient Data  |
+-----------+------------+
            |
            v
+------------------------+
|     DOCTOR APP         |
| Doctor Dashboard       |
| Patients / Alerts      |
| Notes / Status         |
+------------------------+
________________________________________
Application Workflow
Patient Workflow
User Opens App
      |
      v
Select Urine or Stool Analysis
      |
      v
Capture Image / Choose from Gallery
      |
      v
TensorFlow Lite Model Processes Image
      |
      v
Display Preliminary Result
      |
      v
Save Analysis History
      |
      +--------------------------+
      |                          |
      v                          v
GI Health Assistant       Urgent Doctor Alert
                                 |
                                 v
                              Firebase
________________________________________
Doctor Workflow
Doctor Login
     |
     v
Firebase Role Check
     |
     v
Doctor Dashboard
     |
     +-------------------+
     |                   |
     v                   v
View Patients       View Urgent Alerts
                         |
                         v
                    Open Alert
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
     Add Note      Acknowledge      Resolve
                         |
                         v
                  View Patient History
________________________________________
Machine Learning Model
The image-analysis component is based on a Convolutional Neural Network (CNN).
The model was trained using Python and TensorFlow.
A typical model structure includes:
Input Image
    |
    v
Rescaling
    |
    v
Conv2D
    |
    v
MaxPooling
    |
    v
Conv2D
    |
    v
MaxPooling
    |
    v
Conv2D
    |
    v
MaxPooling
    |
    v
Flatten
    |
    v
Dense Layer
    |
    v
Output Classification
After training, the model was converted into TensorFlow Lite format for mobile deployment.
________________________________________
TensorFlow Lite Integration
The Android application loads the .tflite model locally.
A selected or captured image is:
1.	Converted into a bitmap.
2.	Resized to the input size expected by the model.
3.	Converted into the required tensor format.
4.	Passed into the TensorFlow Lite model.
5.	Classified.
6.	Returned as a readable prediction to the user.
Because inference occurs locally on the device, the application does not require a dedicated machine-learning server for image classification.
________________________________________
Chatbot Architecture
The gastrointestinal health chatbot uses a retrieval-based question-answering approach.
A health-related PDF document is processed and divided into smaller sections.
These sections are transformed into embeddings and stored in a FAISS vector database.
When a user asks a question:
User Question
      |
      v
LangChain
      |
      v
FAISS Vector Search
      |
      v
Relevant Document Sections
      |
      v
Language Model
      |
      v
Generated Response
The chatbot interface is deployed using Streamlit.
________________________________________
Project Structure
A typical project structure may resemble:
project/
│
├── android-app/
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/
│   │   │   │   ├── res/
│   │   │   │   │   ├── layout/
│   │   │   │   │   ├── drawable/
│   │   │   │   │   ├── values/
│   │   │   │   │   └── mipmap/
│   │   │   │   ├── assets/
│   │   │   │   │   └── model.tflite
│   │   │   │   └── AndroidManifest.xml
│
├── chatbot/
│   ├── app.py
│   ├── requirements.txt
│   ├── .env
│   └── knowledge-base.pdf
│
├── machine-learning/
│   ├── model_training.ipynb
│   ├── model_conversion.ipynb
│   └── dataset/
│
└── README.md
The exact project structure may differ depending on the current implementation.
________________________________________
Installation
Android Application
Clone the repository:
git clone <repository-url>
Open the Android project using Android Studio.
Allow Gradle to download the required dependencies.
Connect an Android device or start an emulator.
Run the application.
________________________________________
Chatbot Installation
Navigate to the chatbot directory:
cd chatbot
Create a Python virtual environment:
python -m venv venv
Activate it on Windows:
venv\Scripts\activate
Install the required dependencies:
pip install streamlit pypdf2 langchain python-dotenv faiss-cpu openai
Run the chatbot:
streamlit run app.py
________________________________________
Environment Variables
API keys should never be uploaded directly to GitHub.
Create a .env file for sensitive information.
Example:
OPENAI_API_KEY=your_api_key_here
Add .env to .gitignore.
.env
You should also avoid uploading:
•	Firebase service credentials
•	Private API keys
•	Passwords
•	Secret configuration files
•	User health information
________________________________________
Security and Privacy Considerations
Because this project handles health-related information, privacy and security should be considered carefully.
Recommended practices include:
•	Secure Firebase Authentication
•	Role-based authorization
•	Restricted Firebase security rules
•	Secure transmission of data
•	No API keys stored directly in source code
•	Limited storage of personally identifiable information
•	Secure access to patient alerts
•	Proper user consent
•	Clear medical disclaimers
Future production versions should undergo formal security and privacy review before handling real patient information.
________________________________________
Current Limitations
Some limitations of the current system include:
•	AI predictions depend heavily on image quality.
•	Lighting conditions can affect image classification.
•	The model is not a substitute for laboratory testing.
•	Historical patient images are not currently synchronized to doctors.
•	Background doctor notifications are not yet enabled.
•	Patient-to-doctor messaging is not currently available.
•	Clinical validation is required before real-world medical use.
•	Larger and more diverse datasets may be required to improve model accuracy.
________________________________________
Future Improvements
Possible future improvements include:
•	Firebase Cloud Messaging for urgent doctor notifications
•	Patient-to-doctor messaging
•	QR-code doctor connection
•	Patient invitation and approval workflow
•	Image synchronization through Firebase Storage
•	Improved AI model accuracy
•	Larger and more diverse training datasets
•	Explainable AI features
•	More detailed health recommendations
•	Secure cloud backups
•	Advanced doctor analytics dashboard
•	Alert filtering and search
•	Dark mode
•	Improved accessibility
•	Offline chatbot support
•	Multi-language support
________________________________________
UI Design
The application's interface follows a modern healthcare-oriented design.
The patient interface focuses on:
•	Simple navigation
•	Clear analysis actions
•	Easy-to-understand results
•	Accessible health information
•	Calm and reassuring presentation
The doctor interface focuses on:
•	Fast access to alerts
•	Patient monitoring
•	Clear status indicators
•	Efficient review of patient histories
•	Professional clinical presentation
________________________________________
Intended Use
This project is intended primarily for:
•	Academic research
•	Final-year university project demonstration
•	Machine-learning experimentation
•	Android development learning
•	Healthcare technology research
•	Portfolio demonstration
It is not intended to be used as a certified medical diagnostic system without additional clinical testing, regulatory approval, security review, and medical validation.
________________________________________
Author
Jeffrey Nsiah Ackah
BSc Information and Communication Technology
________________________________________
License
This project is intended for educational and research purposes.
A suitable open-source license such as the MIT License may be added depending on how the repository is intended to be shared.
________________________________________
Acknowledgement
This project combines mobile application development, artificial intelligence, machine learning, conversational AI, and cloud technologies to explore how modern digital tools can support early health awareness and improve communication between patients and healthcare professionals.
