ShoeTA 👟

A comprehensive mobile job marketplace designed to bridge the gap between shoe factory recruiters and workers. Factories can post jobs and manage applicants, workers can search and apply for jobs, and admins oversee the entire platform — including verifying new factories before they go live.


📱 App Screenshots & User Flow

To give you a visual tour of the application, here is the complete user flow across all three roles — Worker, Factory, and Admin:

1. Onboarding & Authentication

Access the platform by choosing a role. New factories and workers can register, existing users log in with their credentials. Factory registrations are held for admin approval before they can access the platform.

<img width="120" height="200" alt="mainActivity" src="https://github.com/user-attachments/assets/b28373b9-746e-4d7e-bb96-0b3a48682151" />

---

2. Worker Dashboard & Job Discovery

Once logged in, workers see featured jobs, can filter by job type or shift, and search by title or location.

| Home | Profile | ListJob| MyApplications| SavedJobs| Notifications|
| :---: | :---: | :---: | :---:| :---: | :---: |
| <img width="120" height="200" alt="home_wrkr" src="https://github.com/user-attachments/assets/214614c3-2adc-46d7-bd51-daa677bfe9bc" /> | <img width="120" height="200" alt="worker_profile" src="https://github.com/user-attachments/assets/41c3b955-de15-42b7-b8e0-76eaabaf52c0" /> | <img width="120" height="200" alt="all_jobs_wrkr" src="https://github.com/user-attachments/assets/f6698590-66ca-48e6-89bd-8f93bea2c7e8" />| <img width="120" height="200" alt="my_applications_wrkr" src="https://github.com/user-attachments/assets/cb401f17-d537-4bb0-afa0-d21d3971823c" />| <img width="120" height="200" alt="savedJobs_wrkr" src="https://github.com/user-attachments/assets/c9114195-874e-4e4b-9df8-643aaa3fb34e" />| <img width="120" height="200" alt="noti_wrkr" src="https://github.com/user-attachments/assets/273276e9-b768-4ee1-8651-f7f792114f8d" /> |

---

3. Factory Dashboard & Job Posting

Factories get a quick overview of their posted jobs and total applications, and can post new openings with detailed requirements.

| Factory Dashboard | Post a Job| AllJobs| Filter Applicant with JobTypes | giveAppointment|
| :---: | :---: | :---: | :---: | :---: |
|<img width="120" height="200" alt="factory_dashboard" src="https://github.com/user-attachments/assets/92b7d888-76ed-48f6-81d3-7132116c13f0" /> |<img width="120" height="200" alt="post_job_fac" src="https://github.com/user-attachments/assets/e6d05663-c109-4d8a-a388-bdc0e0cfc786" /> | <img width="120" height="200" alt="allJobs_factory" src="https://github.com/user-attachments/assets/abb527ed-5e69-42e1-be6f-9b0516c618a5" /> |<img width="120" height="200" alt="job_type_withApplicants" src="https://github.com/user-attachments/assets/548f2ff2-7180-4b01-8a83-1022ae098943" /> |<img width="120" height="200" alt="appoinmentFac" src="https://github.com/user-attachments/assets/b3e5b143-26e2-4539-8579-f3b980afdda5" /> |

---


5. Admin Panel

Admins manage job titles/shifts, view platform-wide stats, and approve or reject new factory registrations.

Admin Dashboard| Manage job | FactoriesApprove/Reject| 
| :---: | :---: | :---: |
|<img width="120" height="200" alt="admin_dashboard" src="https://github.com/user-attachments/assets/252ec8b9-a391-43f7-8c78-13078308bc90" /> |<img width="120" height="200" alt="approval_waiting" src="https://github.com/user-attachments/assets/24519423-f6cd-4944-ae07-e25135d95d38" /> |<img width="120" height="200" alt="approval" src="https://github.com/user-attachments/assets/fdaf7884-e587-4904-8a8e-d83285348279" />|

---

✨ Features


Three-Role Authentication: Separate login flows for Workers, Factories, and Admins.
Factory Approval Workflow: New factory registrations stay pending until an admin reviews and approves them — factories can't log in until approved.
Smart Job Filtering: Filter jobs by type (Full-time/Part-time), shift (Day/Night), title, and location.
Save & Track Applications: Bookmark jobs for later and track every application's status (Pending, Reviewed, Accepted, Rejected).
Bulk Appointment Scheduling: Factories can select multiple applicants at once and schedule interview appointments in a single action.
Push Notifications: Workers get notified instantly when their application is accepted or an appointment is scheduled, via Firebase Cloud Messaging.
Admin Controls: Manage job titles, shifts, and job types platform-wide; approve or reject factory accounts.
In-App Ads: AdMob banner integration for monetization.



🛠️ Technology Stack


Frontend: Android (Java), Volley for networking, Glide for image loading
Backend: PHP (mysqli, prepared statements)
Database: MySQL
Push Notifications: Firebase Cloud Messaging (FCM HTTP v1)
Ads: Google AdMob



🚀 Getting Started

To get a local copy up and running, follow these simple steps:

Prerequisites


Android Studio with a JDK 11+ setup
A MySQL database and PHP-capable web host
A Firebase project (for FCM)


Installation


Clone the repository:


bash   git clone https://github.com/<your-username>/ShoeTA.git


Backend setup:

Import the provided SQL schema into your MySQL database.
Copy config.php into your web root and fill in your DB credentials.
Upload all PHP endpoint files (login.php, register.php, get_jobs.php, etc.) to the same directory.
Download a Firebase service account key and place it as service-account.json next to fcm_helper.php (keep this file private — see Security below).



Android setup:

Open the project in Android Studio.
Download google-services.json from your Firebase project and place it in app/.
Update the BASE_URL constant in each Activity if your backend is hosted elsewhere.
Build and run.






🔒 Security


google-services.json and service-account.json are excluded from version control via .gitignore — never commit these.
The Android API key should be restricted in Google Cloud Console to your app's package name and SHA-1 signing fingerprint.
All database queries use prepared statements; passwords are hashed with bcrypt.



📋 About

An Android Studio app built using Java, PHP, and MySQL to connect shoe factory recruiters with workers, with an admin-moderated approval system for factories.
