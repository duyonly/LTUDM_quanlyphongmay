Đồ án : ứng dụng quản lý phòng máy
-người 1:duy
-người 2:danh
-người 3: trí
1/sơ đồ luồng hoạt động:
                  ┌─────────────────────┐
                  │      DATABASE       │
                  │    PostgreSQL       │
                  └──────────▲──────────┘
                             │
                             │
                  ┌──────────┴──────────┐
                  │       SERVER        │
                  │                     │
                  │ - Authentication    │
                  │ - Session Manager   │
                  │ - Command Relay     │
                  │ - Logging           │
                  │ - Machine Manager   │
                  └───────▲───────▲─────┘
                          │       │
                   TCP/TLS│       │TCP/TLS
                          │       │
             ┌────────────┘       └────────────┐
             │                                 │
    ┌────────▼────────┐               ┌────────▼────────┐
    │ ADMIN CLIENT    │               │ MACHINE CLIENT  │
    │                 │               │                 │
    │ JavaFX GUI      │               │ JavaFX GUI      │
    │                 │               │                 │
    │ - Danh sách máy │               │ - Trạng thái    │
    │ - Monitor       │               │ - Bị điều khiển │
    │ - Control       │               │ - Nhận lệnh     │
    │ - Broadcast     │               │ - Gửi màn hình  │
•  Server = trung tâm giao thông.
•  Admin = người quản lý.
•  Machine Client/Agent = chương trình chạy trên từng máy phòng máy.
•  Database = lưu tài khoản, máy, lịch sử.
2/các công nghệ dùng trong đồ án:
Thành phần	Công nghệ
Admin Client	JavaFX
Machine Client	JavaFX
Server	Java
Network	TCP Socket / SSLSocket
Protocol	JSON tự thiết kế
Database	PostgreSQL
ORM/DB	JDBC hoặc JPA
Password	BCrypt
System info	OSHI
Screenshot	java.awt.Robot
Remote keyboard/mouse	java.awt.Robot
Build	Maven
Git	GitHub


TUẦN 1 — Thiết kế + Socket cơ bản
🎯 Mục tiêu
Cuối tuần phải đạt:
          ┌───────────┐
          │  SERVER   │
          └─────┬─────┘
             ↑  │  ↓
       ┌─────┘  │  └─────┐
       │         │        │
    ADMIN     MACHINE   MACHINE
Admin và Machine đều kết nối được Server.
________________________________________
Ngày 1 — Cả nhóm
Thống nhất kiến trúc.
Vẽ:
1. Architecture
Admin
  ↓
Server
  ↓
Machine
  ↓
Database
2. Use Case
Admin:
Login
Xem máy
Xem thông tin
Xem process
Kill process
Xem màn hình
Lock
Unlock
Shutdown
Logout
Gửi message
Gửi file
Broadcast
Remote control
Machine:
Connect
Send heartbeat
Send system info
Receive command
Execute command
Send screenshot
________________________________________
Ngày 2 — Thiết kế protocol
Đây là việc rất quan trọng.
Thống nhất message.
Ví dụ:
{
  "type": "COMMAND",
  "requestId": "123",
  "command": "GET_SYSTEM_INFO",
  "target": "PC01",
  "data": {}
}
Các command:
GET_SYSTEM_INFO
GET_PROCESSES
KILL_PROCESS
SCREENSHOT
LOCK
UNLOCK
SHUTDOWN
LOGOUT
MESSAGE
FILE
BROADCAST
REMOTE_INPUT
________________________________________
Ngày 3 — GitHub
Tạo repository:
computer-room-management
│
├── server
├── admin-client
├── machine-client
├── common
├── database
└── docs
common chứa:
CommandType
Message
Response
MachineInfo
________________________________________
Ngày 4–5 — Người 1
Làm Server:
ServerSocket
   ↓
accept()
   ↓
ClientHandler
   ↓
ClientManager
Server phải nhận nhiều connection.
Ví dụ:
PC01 connected
PC02 connected
PC03 connected
Admin connected
________________________________________
Ngày 4–5 — Người 2
Machine:
MachineClient
    ↓
connect("server", port)
    ↓
send MACHINE_CONNECT
    ↓
heartbeat
________________________________________
Ngày 4–5 — Người 3
Admin:
AdminClient
    ↓
connect Server
    ↓
Login GUI sơ bộ
________________________________________
Ngày 6 — Ghép
Test:
Machine → Server
Admin → Server
________________________________________
Ngày 7 — Deadline
Bắt buộc chạy được:
Server
 ↑       ↑
Admin   PC01
        PC02
Server biết:
Admin connected
PC01 connected
PC02 connected
Nếu tuần 1 chưa đạt cái này thì tuần 2 chưa làm chức năng khác.
________________________________________
III. TUẦN 2 — Database + Login + Online/Offline
🎯 Mục tiêu
Admin đăng nhập được và nhìn thấy máy.
Login
 ↓
Server
 ↓
PostgreSQL
 ↓
Dashboard
 ↓
PC01 ONLINE
PC02 ONLINE
PC03 OFFLINE
________________________________________
Ngày 1 — Database
Người 1 tạo:
users
id
username
password_hash
role
created_at
machines
id
machine_code
hostname
ip_address
os
status
last_seen
sessions
id
user_id
machine_id
connected_at
disconnected_at
commands
id
machine_id
admin_id
command_type
status
created_at
________________________________________
Ngày 2 — Authentication
Login:
Admin
 ↓
username/password
 ↓
Server
 ↓
Database
 ↓
verify
 ↓
OK
Password lưu dạng hash, không lưu plaintext.
________________________________________
Ngày 3 — Machine registration
Machine gửi:
{
  "type": "MACHINE_CONNECT",
  "data": {
    "machineCode": "PC01",
    "hostname": "LAB-PC01"
  }
}
Server cập nhật:
PC01 = ONLINE
________________________________________
Ngày 4 — Heartbeat
Machine cứ vài giây gửi:
HEARTBEAT
Server cập nhật:
last_seen
Nếu lâu không nhận:
ONLINE → OFFLINE
________________________________________
Ngày 5–6 — Người 3 làm Dashboard
Giao diện:
┌────────────────────────────────────┐
│       QUẢN LÝ PHÒNG MÁY            │
├────────────────────────────────────┤
│ 🟢 PC01     ONLINE                 │
│ 🟢 PC02     ONLINE                 │
│ 🔴 PC03     OFFLINE                │
│ 🟢 PC04     ONLINE                 │
└────────────────────────────────────┘
________________________________________
Ngày 7 — Deadline
Demo:
Login
 ↓
Dashboard
 ↓
Danh sách máy
 ↓
Online / Offline
________________________________________
IV. TUẦN 3 — CPU/RAM/Disk + Process
🎯 Mục tiêu
Click PC01:
CPU: 35%
RAM: 62%
Disk: 45%
OS: Windows 11
Hostname: PC01
và xem process.
________________________________________
Ngày 1 — Protocol
Người 1 thêm:
GET_SYSTEM_INFO
GET_PROCESSES
KILL_PROCESS
________________________________________
Ngày 2–3 — Machine
Người 2 dùng OSHI lấy:
CPU
RAM
Disk
OS
Hostname
Ví dụ:
SystemInfo systemInfo = new SystemInfo();
Sau đó chuyển thành JSON.
________________________________________
Ngày 3–4 — Process
Lấy:
PID
Name
Memory
CPU
Ví dụ UI:
PID      NAME
----------------------
1200     chrome.exe
2330     java.exe
4532     Code.exe
________________________________________
Ngày 4–5 — Kill process
Admin:
Chọn chrome.exe
       ↓
[Kill]
       ↓
Server
       ↓
Machine
       ↓
ProcessManager
________________________________________
Ngày 5–6 — Admin UI
Machine Detail:
┌──────────────────────────────┐
│ PC01                         │
│                              │
│ CPU       35%                │
│ RAM       62%                │
│ Disk      45%                │
│                              │
│ Processes                    │
│ chrome.exe                   │
│ java.exe                     │
│ Code.exe                     │
│                              │
│ [Kill Process]               │
└──────────────────────────────┘
________________________________________
Ngày 7 — Deadline
Phải demo được:
Admin
 ↓
PC01
 ↓
CPU/RAM/Disk
 ↓
Process
 ↓
Kill process
________________________________________
V. TUẦN 4 — Screenshot + Monitor nhiều máy
🎯 Mục tiêu
Đây là tuần quan trọng.
Admin có thể xem:
┌─────────────┐ ┌─────────────┐
│    PC01     │ │    PC02     │
│             │ │             │
│  SCREENSHOT │ │  SCREENSHOT │
└─────────────┘ └─────────────┘

┌─────────────┐ ┌─────────────┐
│    PC03     │ │    PC04     │
│             │ │             │
│  SCREENSHOT │ │  SCREENSHOT │
└─────────────┘ └─────────────┘
________________________________________
Ngày 1 — Screenshot Machine
Người 2 dùng:
Robot
chụp màn hình.
Sau đó:
Screenshot
 ↓
BufferedImage
 ↓
JPEG
 ↓
byte[]
________________________________________
Ngày 2 — Protocol
Người 1 thiết kế:
SCREENSHOT_REQUEST
SCREENSHOT_RESPONSE
Có thể gửi binary hoặc Base64 trong giai đoạn đầu.
Nếu muốn đơn giản để demo, có thể Base64; nếu muốn hiệu quả hơn, thiết kế message có header + binary payload.
________________________________________
Ngày 3–4 — Server
Server:
Admin
 ↓
SCREENSHOT PC01
 ↓
Server
 ↓
PC01
 ↓
Screenshot
 ↓
Server
 ↓
Admin
________________________________________
Ngày 4–5 — Admin
Người 3 làm:
MonitorGrid
Ví dụ:
GridPane
 ├── PC01 ImageView
 ├── PC02 ImageView
 ├── PC03 ImageView
 └── PC04 ImageView
________________________________________
Ngày 6 — Refresh
Ví dụ:
2–5 giây
Machine gửi screenshot mới.
Chưa cần video real-time.
________________________________________
Ngày 7 — Deadline
Phải xem được:
ít nhất 2–3 máy cùng lúc.
Nếu được 4–5 máy càng tốt.
________________________________________
VI. TUẦN 5 — Control + Message + File + Broadcast
🎯 Mục tiêu
Hoàn thiện phần điều khiển cơ bản.
________________________________________
Ngày 1 — Lock
Admin
 ↓
LOCK PC01
 ↓
Server
 ↓
PC01
 ↓
Lock
________________________________________
Ngày 2 — Unlock
Tương tự:
UNLOCK
________________________________________
Ngày 3 — Shutdown + Logout
Command:
SHUTDOWN
LOGOUT
________________________________________
Ngày 4 — Message
Admin nhập:
Máy sẽ được bảo trì lúc 18h.
Machine nhận:
┌────────────────────────┐
│ THÔNG BÁO              │
│                        │
│ Máy sẽ được bảo trì    │
│ lúc 18h.               │
└────────────────────────┘
________________________________________
Ngày 5 — Broadcast
Admin:
Gửi tất cả
Server:
          ┌── PC01
          ├── PC02
Admin → Server ── PC03
          ├── PC04
          └── PC05
________________________________________
Ngày 6 — File
Đơn giản hóa:
Admin
 ↓
Select File
 ↓
Server
 ↓
Machine
 ↓
Save
Làm trước file nhỏ.
________________________________________
Ngày 7 — Deadline
Các chức năng phải chạy:
✓ Lock
✓ Unlock
✓ Shutdown
✓ Logout
✓ Message
✓ Broadcast
✓ File
________________________________________
VII. TUẦN 6 — Remote Keyboard + Mouse + Security
Đây là tuần khó nhất.
🎯 Mục tiêu
Admin:
Xem màn hình PC01
       ↓
Click chuột
       ↓
PC01 nhận
       ↓
thực hiện
và keyboard.
________________________________________
Ngày 1 — Input Protocol
Ví dụ mouse:
{
  "type": "INPUT",
  "inputType": "MOUSE_MOVE",
  "x": 500,
  "y": 300
}
Click:
{
  "type": "INPUT",
  "inputType": "MOUSE_CLICK",
  "button": "LEFT"
}
Keyboard:
{
  "type": "INPUT",
  "inputType": "KEY_DOWN",
  "key": "A"
}
________________________________________
Ngày 2–3 — Machine
Người 2 xử lý bằng Robot.
MOUSE_MOVE
MOUSE_CLICK
KEY_DOWN
KEY_UP
________________________________________
Ngày 3–4 — Admin
Người 3:
RemoteControlView
Hiển thị screenshot.
Bắt event:
MouseEvent
KeyEvent
Sau đó gửi Server.
________________________________________
Ngày 4–5 — Server
Người 1:
INPUT
 ↓
validate
 ↓
check permission
 ↓
route to machine
________________________________________
Ngày 5–6 — TLS
Chuyển:
Socket
sang:
SSLSocket
SSLServerSocket
Kiểm tra:
Admin ← TLS → Server ← TLS → Machine
________________________________________
Ngày 6 — Authorization
Ví dụ:
role = ADMIN
mới được:
KILL_PROCESS
SHUTDOWN
REMOTE_INPUT
Machine thường không được gửi command nguy hiểm ngược lại.
________________________________________
Ngày 7 — Deadline
Demo:
Login
 ↓
chọn PC01
 ↓
xem screen
 ↓
mouse
 ↓
keyboard
và đường truyền đã có TLS.
________________________________________
VIII. TUẦN 7 — Tích hợp + sửa lỗi + báo cáo
⚠️ TUẦN NÀY KHÔNG ĐƯỢC BẮT ĐẦU CHỨC NĂNG LỚN MỚI.
________________________________________
Ngày 1 — Test toàn hệ thống
Chạy:
1 Server
1 Admin
3–5 Machine
Test:
Login
Machine online
Machine offline
System info
Process
Kill process
Screenshot
Monitor
Lock
Unlock
Shutdown
Message
Broadcast
File
Remote mouse
Remote keyboard
________________________________________
Ngày 2 — Test mất kết nối
Ví dụ:
PC01
 ↓
disconnect
Server phải cập nhật:
PC01 → OFFLINE
PC01 reconnect:
PC01 → ONLINE
________________________________________
Ngày 3 — Test nhiều máy
Test:
PC01
PC02
PC03
PC04
PC05
đồng thời.
Đặc biệt kiểm tra:
Admin → PC01
Admin → PC02
Admin → PC03
Không được gửi nhầm command.
________________________________________
Ngày 4 — Security + Database
Kiểm tra:
✓ Password hash
✓ Login
✓ Authorization
✓ TLS
✓ Command log
✓ Session log
________________________________________
Ngày 5 — UI + Bug Fix
Người 3 tập trung:
GUI
button
table
status
error message
loading
Người 1:
Server bug
Database bug
Protocol bug
Người 2:
Machine bug
Screenshot bug
Control bug
________________________________________
Ngày 6 — Báo cáo + Slide
Báo cáo nên có:
1. Giới thiệu đề tài
2. Phân tích yêu cầu
3. Kiến trúc hệ thống
4. Use Case
5. Thiết kế database
6. Thiết kế protocol
7. Thiết kế Server
8. Thiết kế Admin
9. Thiết kế Machine Client
10. Bảo mật
11. Demo
12. Kết quả
13. Hạn chế
14. Hướng phát triển
________________________________________
Ngày 7 — DEADLINE
Không code thêm.
Chỉ:
Demo
Test
Backup
Backup:
GitHub
Database.sql
README.md
Source code
Report.pdf
Slide.pptx
________________________________________
IX. Bảng công việc 7 tuần cho 3 người
Tuần	Người 1 — Server/DB	Người 2 — Machine	Người 3 — Admin
1	TCP Server + Protocol	Connect + Heartbeat	JavaFX + Connect
2	DB + Login + Session	Registration + Online	Login + Dashboard
3	System/Process command	CPU/RAM/Disk/Process	System + Process UI
4	Screenshot routing	Screenshot	Monitor Grid
5	Control commands	Lock/Shutdown/Message/File	Control UI/Broadcast
6	TLS + Authorization + Input routing	Mouse/Keyboard	Remote Control UI
7	Test + Bug + Report	Test + Bug + Report	UI + Test + Slide
________________________________________
X. Mốc kiểm tra để không bị trễ
Mình sẽ đặt 5 mốc cứng:
🔴 Cuối tuần 1
Admin ←→ Server ←→ Machine
🟠 Cuối tuần 2
Login
+
Database
+
Online/offline
🟡 Cuối tuần 4
System info
+
Process
+
Screenshot
+
Monitor
🟢 Cuối tuần 5
Lock
Shutdown
Message
File
Broadcast
🔵 Cuối tuần 6
Remote keyboard/mouse
+
TLS
Nếu tới cuối tuần 5 mà remote control chưa làm được thì vẫn ưu tiên sửa các chức năng bắt buộc đã có, không được vì remote control mà làm hỏng toàn bộ hệ thống.
________________________________________
XI. Cấu trúc project mình khuyên dùng
Nếu dùng Maven, có thể làm dạng:
computer-room-management/
│
├── server/
│   ├── pom.xml
│   └── src/
│
├── admin-client/
│   ├── pom.xml
│   └── src/
│
├── machine-client/
│   ├── pom.xml
│   └── src/
│
├── common/
│   ├── pom.xml
│   └── src/
│       └── main/java/
│           └── com/room/common/
│               ├── Message.java
│               ├── CommandType.java
│               ├── Response.java
│               └── MachineInfo.java
│
├── database/
│   └── schema.sql
│
├── docs/
│   ├── architecture.md
│   ├── protocol.md
│   ├── database.md
│   └── usecase.md
│
└── README.md
common rất quan trọng vì 3 người đều dùng chung protocol.
________________________________________
XII. Cách làm Git để 3 người không đạp code nhau
Ví dụ:
main
│
├── develop
│
├── feature/server
├── feature/machine
└── feature/admin
Mỗi người làm branch riêng.
Ví dụ Người 2:
git checkout -b feature/machine
Xong chức năng:
git add .
git commit -m "feat: add machine heartbeat"
git push
Sau đó merge vào develop.
Không cho cả 3 người sửa trực tiếp main.
________________________________________


