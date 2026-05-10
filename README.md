#  Gym Management System 

Spring Boot RESTful API لإدارة الصالات الرياضية مع JWT Authentication و PostgreSQL.

---

## 🛠️ Tech Stack

| Technology      | Version  |
|----------------|----------|
| Java            | 21      |
| Spring Boot     | 3.2.0    |
| Spring Security | 6.x      |
| PostgreSQL      | Latest   |
| JWT (jjwt)      | 0.11.5   |
| Lombok          | Latest   |
| Maven           | 3.x      |

---

## 📂 Project Structure

```
src/main/java/com/gym/
├── GymSystemApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   ├── MemberController.java
│   ├── TrainerController.java
│   ├── GymClassController.java
│   ├── SubscriptionController.java
│   └── PaymentController.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── MemberRequest.java
│   │   ├── TrainerRequest.java
│   │   ├── GymClassRequest.java
│   │   ├── SubscriptionRequest.java
│   │   └── PaymentRequest.java
│   └── response/
│       ├── ApiResponse.java
│       └── AuthResponse.java
├── entity/
│   ├── User.java
│   ├── Member.java
│   ├── Trainer.java
│   ├── GymClass.java
│   ├── Subscription.java
│   └── Payment.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
├── repository/
│   ├── UserRepository.java
│   ├── MemberRepository.java
│   ├── TrainerRepository.java
│   ├── GymClassRepository.java
│   ├── SubscriptionRepository.java
│   └── PaymentRepository.java
├── security/
│   ├── JwtUtil.java
│   └── JwtAuthFilter.java
└── service/
    ├── AuthService.java
    ├── MemberService.java
    ├── TrainerService.java
    ├── GymClassService.java
    ├── SubscriptionService.java
    └── PaymentService.java
```

---

## ⚙️ Setup

### 1. إنشاء Database
```sql
CREATE DATABASE gym_db;
```

### 2. تعديل application.properties
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gym_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

### 3. تشغيل المشروع
```bash
mvn spring-boot:run
```

---

## 🔐 Authentication

### Roles
| Role    | Permissions                        |
|---------|------------------------------------|
| ADMIN   | كل العمليات                        |
| MANAGER | إنشاء وتعديل وحذف (ما عدا بعض العمليات) |
| COACH  | إنشاء وتعديل الأعضاء والمدفوعات    |

---

## 📋 API Endpoints

### 🔑 Auth `/api/auth`
| Method | Endpoint    | Description      | Auth Required |
|--------|-------------|------------------|---------------|
| POST   | /register   | تسجيل مستخدم جديد | ❌            |
| POST   | /login      | تسجيل الدخول      | ❌            |

**Register Request:**
```json
{
  "username": "admin",
  "password": "admin123",
  "email": "admin@gym.com",
  "role": "ADMIN"
}
```

**Login Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "username": "admin",
    "role": "ADMIN"
  }
}
```

---

### 👤 Members `/api/members`
| Method | Endpoint           | Description         | Auth       |
|--------|--------------------|---------------------|------------|
| POST   | /                  | إضافة عضو جديد       | STAFF+     |
| GET    | /                  | جميع الأعضاء         | Any        |
| GET    | /{id}              | عضو بالـ ID          | Any        |
| PUT    | /{id}              | تعديل بيانات عضو     | STAFF+     |
| DELETE | /{id}              | حذف عضو              | MANAGER+   |
| GET    | /search?query=     | بحث في الأعضاء       | Any        |
| PATCH  | /{id}/status       | تغيير حالة العضو     | MANAGER+   |

**Member Request:**
```json
{
  "firstName": "Ahmed",
  "lastName": "Ali",
  "email": "ahmed@example.com",
  "phone": "01234567890",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "address": "Cairo, Egypt"
}
```

---

### 🏃 Trainers `/api/trainers`
| Method | Endpoint   | Description          | Auth     |
|--------|------------|----------------------|----------|
| POST   | /          | إضافة مدرب جديد       | MANAGER+ |
| GET    | /          | جميع المدربين         | Any      |
| GET    | /active    | المدربين النشطين      | Any      |
| GET    | /{id}      | مدرب بالـ ID          | Any      |
| PUT    | /{id}      | تعديل بيانات مدرب     | MANAGER+ |
| DELETE | /{id}      | حذف مدرب              | ADMIN    |

**Trainer Request:**
```json
{
  "firstName": "Mohamed",
  "lastName": "Hassan",
  "email": "trainer@gym.com",
  "phone": "01098765432",
  "specialization": "Weightlifting",
  "bio": "10 years of experience",
  "salaryPerHour": 150.0
}
```

---

### 🧘 Classes `/api/classes`
| Method | Endpoint                        | Description           | Auth     |
|--------|---------------------------------|-----------------------|----------|
| POST   | /                               | إنشاء حصة جديدة       | MANAGER+ |
| GET    | /                               | جميع الحصص            | Any      |
| GET    | /{id}                           | حصة بالـ ID            | Any      |
| PUT    | /{id}                           | تعديل حصة             | MANAGER+ |
| DELETE | /{id}                           | حذف حصة               | MANAGER+ |
| POST   | /{classId}/enroll/{memberId}    | تسجيل عضو في حصة      | Any      |
| DELETE | /{classId}/unenroll/{memberId}  | إلغاء تسجيل عضو       | Any      |
| GET    | /trainer/{trainerId}            | حصص مدرب معين         | Any      |

**GymClass Request:**
```json
{
  "name": "Yoga Morning",
  "description": "Relaxing yoga session",
  "startTime": "2025-06-01T08:00:00",
  "endTime": "2025-06-01T09:00:00",
  "capacity": 20,
  "trainerId": 1
}
```

---

### 📋 Subscriptions `/api/subscriptions`
| Method | Endpoint              | Description             | Auth     |
|--------|-----------------------|-------------------------|----------|
| POST   | /                     | إنشاء اشتراك جديد        | STAFF+   |
| GET    | /                     | جميع الاشتراكات          | Any      |
| GET    | /{id}                 | اشتراك بالـ ID            | Any      |
| GET    | /member/{memberId}    | اشتراكات عضو معين        | Any      |
| GET    | /active               | الاشتراكات النشطة        | Any      |
| GET    | /expired              | الاشتراكات المنتهية      | Any      |
| PATCH  | /{id}/cancel          | إلغاء اشتراك             | MANAGER+ |

**Subscription Plans:** `MONTHLY | QUARTERLY | SEMI_ANNUAL | ANNUAL`

**Subscription Request:**
```json
{
  "memberId": 1,
  "plan": "MONTHLY",
  "startDate": "2025-06-01",
  "endDate": "2025-07-01",
  "price": 300.0
}
```

---

### 💳 Payments `/api/payments`
| Method | Endpoint                     | Description          | Auth     |
|--------|------------------------------|----------------------|----------|
| POST   | /                            | إنشاء دفعة           | STAFF+   |
| GET    | /                            | جميع الدفعات          | Any      |
| GET    | /{id}                        | دفعة بالـ ID          | Any      |
| GET    | /member/{memberId}           | دفعات عضو معين       | Any      |
| PATCH  | /{id}/complete               | تأكيد الدفع           | STAFF+   |
| PATCH  | /{id}/refund                 | استرجاع الدفع         | MANAGER+ |
| GET    | /revenue/total               | إجمالي الإيرادات      | MANAGER+ |
| GET    | /revenue/range?from=&to=     | إيرادات بفترة زمنية   | MANAGER+ |

**Payment Methods:** `CASH | CREDIT_CARD | DEBIT_CARD | BANK_TRANSFER | ONLINE`

**Payment Request:**
```json
{
  "memberId": 1,
  "subscriptionId": 1,
  "amount": 300.0,
  "paymentMethod": "CASH",
  "notes": "Monthly subscription payment"
}
```

---

## 🌐 How to Use with Postman

1. **Register** → `POST /api/auth/register`
2. **Login** → `POST /api/auth/login` → احفظ الـ token
3. في كل request تانية، أضف header:
   ```
   Authorization: Bearer <YOUR_TOKEN>
   ```

---

## 📊 Database Schema

```
users          → id, username, password, email, role
members        → id, firstName, lastName, email, phone, status...
trainers       → id, firstName, lastName, specialization, salary...
gym_classes    → id, name, startTime, endTime, capacity, trainer_id...
subscriptions  → id, member_id, plan, startDate, endDate, price, status
payments       → id, member_id, subscription_id, amount, method, status
class_enrollments → class_id, member_id  (many-to-many)
```
