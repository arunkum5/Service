# GVD Auto World — Technical Architecture & Web Migration Specification

> **Project:** GVD Auto World Garage Management System  
> **Workshop Location:** Vibgyor High School Road, GJM Sai Garden Layout, Kundalahalli, Bengaluru, Karnataka 560037  
> **Google Maps Link:** [https://maps.app.goo.gl/xgdEGHBKRth1TUrs7](https://maps.app.goo.gl/xgdEGHBKRth1TUrs7)  
> **Coordinates:** `12.9551796, 77.717493`  
> **Purpose:** Comprehensive technical dump, domain models, database schema, business logic, API specs, and full-stack web implementation roadmap to migrate from Android (Kotlin/Jetpack Compose/Room) to a modern Web Application (Next.js / React / Node.js / PostgreSQL).

---

## Table of Contents
1. [Executive Summary & Core Workflows](#1-executive-summary--core-workflows)
2. [Domain Architecture & Enums](#2-domain-architecture--enums)
3. [Relational Database Schema & Prisma ORM Models](#3-relational-database-schema--prisma-orm-models)
4. [Core Functional Modules & Business Logic](#4-core-functional-modules--business-logic)
   - 4.1 [Role-Based Access Control (RBAC)](#41-role-based-access-control-rbac)
   - 4.2 [Job Card Lifecycle & Creation Wizard](#42-job-card-lifecycle--creation-wizard)
   - 4.3 [6-Point Dent & Scratch Photo Inspection System](#43-6-point-dent--scratch-photo-inspection-system)
   - 4.4 [Main Inventory Engine & Excel/CSV Batch Inward](#44-main-inventory-engine--excelcsv-batch-inward)
   - 4.5 [360° Multi-Point Digital Inspection (Mechanic Mode)](#45-360-multi-point-digital-inspection-mechanic-mode)
   - 4.6 [Customer Tracking Portal, Payments & QR Pass](#46-customer-tracking-portal-payments--qr-pass)
   - 4.7 [Admin Oversight, Financial Analytics & Automated Reminders](#47-admin-oversight-financial-analytics--automated-reminders)
5. [REST / GraphQL / tRPC API Specification](#5-rest--graphql--trpc-api-specification)
6. [Target Web Tech Stack & Architecture](#6-target-web-tech-stack--architecture)
7. [Step-by-Step Migration Plan](#7-step-by-step-migration-plan)

---

## 1. Executive Summary & Core Workflows

The GVD Auto World application is a full-cycle automotive service management ERP designed for two-wheeler and four-wheeler multi-brand garages in India.

### Key Pillars:
1. **Intake & Job Card Creation:** 3-step intake capturing vehicle registration, customer profile, odometer, fuel %, accessories checklist, and mandatory **6-angle body dent inspection photos**.
2. **Shop Floor & Technician Work Order:** Digital checklist with 40+ inspection items (`GOOD`, `NEED_TO_REPLACE`, `SERVICED`) with photographic evidence.
3. **Inventory & Spares Management:** Real-time warehouse tracking with live valuation and **bulk Excel/CSV inward invoice upload**.
4. **Billing & Invoicing:** GST tax calculations (18% standard, split into 9% CGST + 9% SGST), labour vs parts breakdown, advance payment deduction, and balance tracking.
5. **Customer Experience:** Real-time web tracking link, WhatsApp notification triggers, PDF/printable invoice, vehicle pass QR code, and customer rating/review engine.
6. **Executive Dashboard:** Live daily/monthly gross revenue, pending payments, technician productivity, and WhatsApp service reminder automation.

---

## 2. Domain Architecture & Enums

### 2.1 User Roles
```typescript
export enum UserRole {
  SERVICE_ADVISOR = "SERVICE_ADVISOR", // Intake, estimate creation, customer communication
  TECHNICIAN = "TECHNICIAN",           // Job inspection, service actions, photo proof
  CUSTOMER = "CUSTOMER",               // View live status, approve estimate, pay online, write reviews
  ADMIN = "ADMIN"                      // Financial oversight, bulk inventory, auto reminders, replies
}
```

### 2.2 Vehicle Types
```typescript
export enum VehicleType {
  TWO_WHEELER = "TWO_WHEELER",   // Bikes, Scooters, Superbikes
  FOUR_WHEELER = "FOUR_WHEELER"  // Hatchbacks, Sedans, SUVs, Commercial Light
}
```

### 2.3 Job Card Statuses (State Machine)
```typescript
export enum JobCardStatus {
  DRAFT = "DRAFT",                       // Intake in progress
  CHECKED_IN = "CHECKED_IN",             // Vehicle received, parked in bay
  ESTIMATE_PENDING = "ESTIMATE_PENDING", // Spares & labour assigned, waiting for customer sign-off
  IN_PROGRESS = "IN_PROGRESS",           // Mechanic assigned, work ongoing
  PARTS_WAITING = "PARTS_WAITING",       // On hold for spare delivery
  INSPECTION_DONE = "INSPECTION_DONE",   // 360° health check verified
  READY_FOR_DELIVERY = "READY_FOR_DELIVERY", // Washing, detailing done, ready for pickup
  DELIVERED = "DELIVERED",               // Handed over to customer, payment settled
  CANCELLED = "CANCELLED"                // Service cancelled
}
```

### 2.4 Inventory & Spares Category
```typescript
export enum ItemCategory {
  SPARE = "SPARE",         // Hard mechanical/electrical parts (e.g. Brake pads, Spark plug)
  LUBE = "LUBE",           // Oils, coolants, brake fluids, grease
  DETAILING = "DETAILING", // Shampoo, ceramic polish, foam wash, tyre dresser
  LABOUR = "LABOUR"        // Service labor charges, lathe work, diagnostics
}
```

### 2.5 Inspection Health Status
```typescript
export enum InspectionStatus {
  GOOD = "GOOD",                     // Green: Component in optimal working condition
  NEED_TO_REPLACE = "NEED_TO_REPLACE", // Red: Critical wear, replacement required
  SERVICED = "SERVICED"              // Orange: Cleaned, lubed, tightened, or repaired
}
```

### 2.6 Dent Damage Severity
```typescript
export enum DentSeverity {
  NO_DENT = "NO_DENT",             // Clean surface
  MINOR_SCRATCH = "MINOR_SCRATCH", // Surface scratch / swirl
  MEDIUM_DENT = "MEDIUM_DENT",     // Visible depression without sheet metal rupture
  MAJOR_DAMAGE = "MAJOR_DAMAGE"    // Heavy tear, structural damage, broken panel
}
```

---

## 3. Relational Database Schema & Prisma ORM Models

Below is the complete **Prisma Schema** (`schema.prisma`) replicating the Android Room SQLite entities with relational foreign keys.

```prisma
datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

generator client {
  provider = "prisma-client-js"
}

// -------------------------------------------------------------
// 1. VEHICLE PROFILES
// -------------------------------------------------------------
model Vehicle {
  id               String       @id @default(uuid())
  vehicleNumber    String       @unique // e.g., "KA 01 MJ 5821" (normalized uppercase without spaces)
  vehicleType      VehicleType  @default(TWO_WHEELER)
  make             String       // e.g., "Royal Enfield", "Hyundai"
  model            String       // e.g., "Classic 350", "Creta SX"
  year             Int?
  fuelType         String?      // Petrol, Diesel, EV, CNG
  color            String?
  customerName     String
  customerMobile   String
  customerEmail    String?
  address          String?
  lastOdometerKm   Int          @default(0)
  createdAt        DateTime     @default(now())
  updatedAt        DateTime     @updatedAt

  jobCards         JobCard[]
  reminders        ServiceReminder[]
  reviews          CustomerReview[]
}

// -------------------------------------------------------------
// 2. JOB CARDS (CORE WORK ORDER)
// -------------------------------------------------------------
model JobCard {
  id                   String         @id @default(uuid())
  jobCardNumber        String         @unique // e.g., "GVD-2026-1001"
  vehicleId            String
  vehicle              Vehicle        @relation(fields: [vehicleId], references: [id], onDelete: Cascade)
  vehicleNumber        String
  vehicleType          VehicleType    @default(TWO_WHEELER)
  customerName         String
  customerMobile       String
  customerEmail        String?

  odometerKm           Int            @default(0)
  fuelLevelPercent     Int            @default(50) // 0 - 100%
  accessoriesNotes     String         @default("") // Tool kit, First aid, Mobile holder, Helmet
  customerVoice        String         // Problem statement reported by owner
  dentNotes            String         @default("") // Summary text
  
  // JSON array storing the 6 inspection angles (see 6-Point Dent spec below)
  dentPhotosJson       Json           @default("[]") 
  
  assignedTechnician   String         @default("Lead Mechanic Raju")
  status               JobCardStatus  @default(CHECKED_IN)
  
  // Billing amounts
  totalSpares          Float          @default(0.0)
  totalLabour          Float          @default(0.0)
  totalLubes           Float          @default(0.0)
  discountAmount       Float          @default(0.0)
  gstPercent           Float          @default(18.0) // 18% standard GST
  gstAmount            Float          @default(0.0)
  totalAmount          Float          @default(0.0)
  advancePaid          Float          @default(0.0)
  balanceAmount        Float          @default(0.0)
  
  isPaidOnline         Boolean        @default(false)
  paymentMode          String?        // "UPI_RAZORPAY", "CASH", "POS_CARD"
  paymentTransactionId String?
  paymentDate          DateTime?
  
  deliveryDateTime     String         @default("Today, 6:30 PM")
  workshopLocation     String         @default("Vibgyor High School Road, GJM Sai Garden Layout, Kundalahalli, Bengaluru 560037")
  workshopMapsUrl      String         @default("https://maps.app.goo.gl/xgdEGHBKRth1TUrs7")
  
  createdAt            DateTime       @default(now())
  updatedAt            DateTime       @updatedAt

  items                JobCardItem[]
  inspections          VehicleInspectionItem[]
}

// -------------------------------------------------------------
// 3. JOB CARD LINE ITEMS (SPARES & LABOUR)
// -------------------------------------------------------------
model JobCardItem {
  id           String       @id @default(uuid())
  jobCardId    String
  jobCard      JobCard      @relation(fields: [jobCardId], references: [id], onDelete: Cascade)
  inventoryId  String?      // Reference to master inventory if spare/lube
  inventory    InventoryItem? @relation(fields: [inventoryId], references: [id])
  
  itemCategory ItemCategory @default(SPARE)
  itemName     String
  itemNumber   String       @default("") // Part SKU
  quantity     Int          @default(1)
  unitPrice    Float        @default(0.0)
  gstPercent   Float        @default(18.0)
  totalPrice   Float        @default(0.0)
  isApproved   Boolean      @default(true)
  createdAt    DateTime     @default(now())
}

// -------------------------------------------------------------
// 4. MASTER INVENTORY / WAREHOUSE
// -------------------------------------------------------------
model InventoryItem {
  id                String       @id @default(uuid())
  partName          String
  partNumber        String       @unique // SKU
  category          ItemCategory @default(SPARE)
  compatibleType    VehicleType  @default(TWO_WHEELER)
  unitPrice         Float        @default(0.0) // MRP
  costPrice         Float        @default(0.0) // Wholesale purchase price
  stockQuantity     Int          @default(0)
  minThresholdAlert Int          @default(5)
  unit              String       @default("pcs") // pcs, litres, sets, can
  locationRack      String?      // e.g. "Rack-A2, Shelf-4"
  createdAt         DateTime     @default(now())
  updatedAt         DateTime     @updatedAt

  jobCardItems      JobCardItem[]
}

// -------------------------------------------------------------
// 5. INWARD PURCHASE ORDERS (EXCEL/CSV AUDIT LOG)
// -------------------------------------------------------------
model PurchaseOrder {
  id              String       @id @default(uuid())
  poNumber        String       @unique // e.g. "PO-2026-10-881"
  supplierName    String       // e.g. "Bosch South India Depot"
  invoiceNumber   String       // Vendor tax invoice number
  totalAmount     Float        @default(0.0)
  totalUnits      Int          @default(0)
  status          String       @default("RECEIVED") // PENDING, RECEIVED, CANCELLED
  itemsJson       Json         // Raw or normalized array of parts received
  receivedAt      DateTime     @default(now())
  createdAt       DateTime     @default(now())
}

// -------------------------------------------------------------
// 6. 360° VEHICLE INSPECTION CHECKLIST
// -------------------------------------------------------------
model VehicleInspectionItem {
  id           String            @id @default(uuid())
  jobCardId    String
  jobCard      JobCard           @relation(fields: [jobCardId], references: [id], onDelete: Cascade)
  categoryName String            // "Engine & Transmission", "Braking System", "Suspension & Tyres", "Electricals"
  itemName     String            // "Front Disc Brake Pads", "Engine Oil Viscosity", "Chain Slackness"
  status       InspectionStatus  @default(GOOD)
  notes        String            @default("")
  photoUrl     String?           // Photo evidence uploaded by mechanic
  updatedBy    String?           // Technician name
  updatedAt    DateTime          @updatedAt
}

// -------------------------------------------------------------
// 7. AUTOMATED SERVICE REMINDERS (WHATSAPP/SMS)
// -------------------------------------------------------------
model ServiceReminder {
  id                  String    @id @default(uuid())
  vehicleId           String?
  vehicle             Vehicle?  @relation(fields: [vehicleId], references: [id], onDelete: SetNull)
  vehicleNumber       String
  customerName        String
  customerMobile      String
  reminderType        String    // "PERIODIC_SERVICE", "CHAIN_LUBRICATION", "BRAKE_PADS_RENEWAL", "GENERAL_CHECKUP"
  dueDate             String    // "YYYY-MM-DD"
  status              String    @default("PENDING") // "PENDING", "SENT", "DISMISSED"
  isWhatsAppTriggered Boolean   @default(false)
  createdAt           DateTime  @default(now())
  sentAt              DateTime?
}

// -------------------------------------------------------------
// 8. CUSTOMER REVIEWS & RATINGS
// -------------------------------------------------------------
model CustomerReview {
  id                String    @id @default(uuid())
  vehicleId         String?
  vehicle           Vehicle?  @relation(fields: [vehicleId], references: [id], onDelete: SetNull)
  vehicleNumber     String
  customerName      String
  rating            Int       // 1 to 5 Stars
  serviceCategory   String    // "General Service", "Brake Overhaul", "Ceramic Coating"
  reviewText        String
  adminResponse     String?   // Admin official reply
  adminRespondedAt  DateTime?
  createdAt         DateTime  @default(now())
}
```

---

## 4. Core Functional Modules & Business Logic

### 4.1 Role-Based Access Control (RBAC)

The application features 4 operational views:

| Role | Permitted Actions |
|---|---|
| **Service Advisor** | Create Job Cards, estimate pricing, assign technicians, update job stages, trigger delivery notifications. |
| **Technician** | View assigned work orders, update 360° health checklist item-by-item (`GOOD` / `NEED_TO_REPLACE` / `SERVICED`), upload photo evidence of repairs. |
| **Customer** | View live vehicle progress bar (Bay intake -> Parts -> Service -> Wash -> Ready), view digital invoice, pay balance online via Razorpay/UPI, download Vehicle QR Pass, submit 1–5 star reviews. |
| **Admin** | Real-time financial analytics (Daily & monthly revenue, online vs counter collections), **Bulk Excel/CSV parts import to Main Inventory**, manual stock increment/decrement, dispatching automated WhatsApp service reminders, responding to customer reviews. |

---

### 4.2 Job Card Lifecycle & Creation Wizard

The job card intake follows a strict 3-step wizard:

```
[ Step 1: Vehicle & Customer Profile ]
  ├── Vehicle Registration (e.g., KA 01 MJ 5821)
  ├── Vehicle Type: TWO_WHEELER or FOUR_WHEELER
  ├── Make & Model (e.g., Royal Enfield Classic 350 / Hyundai Creta)
  ├── Customer Full Name & Mobile Number (10 digits)
  └── Customer Email & Address

[ Step 2: Odometer, Condition & 6 Dent Inspection Photos ]
  ├── Current Odometer (KM)
  ├── Fuel Level Slider (0% - 100%)
  ├── Vehicle Inventory Checklist (Tool kit, first aid, mirrors, mobile mount)
  ├── Customer Voice / Problem Description (multi-line)
  └── 6-Angle Body Dent Inspection Photos (MANDATORY MODULE)

[ Step 3: Spares, Lubricants & Labour Assignment ]
  ├── Search from Main Warehouse Inventory
  ├── Add Spares (Brake pads, Filters, Spark plugs)
  ├── Add Lubes (Engine oil, Coolant, Brake fluid)
  ├── Add Labour services (General service charge, Brake cleaning, Water wash)
  ├── Advance Payment Received (Cash/UPI)
  └── System generates Tax Breakdown (18% GST = 9% CGST + 9% SGST)
```

#### GST Calculation Formula:
```typescript
function calculateJobCardTotals(items: JobCardItem[], advancePaid: number) {
  let totalSpares = 0;
  let totalLabour = 0;
  let totalLubes = 0;

  for (const item of items) {
    const itemTotal = item.quantity * item.unitPrice;
    if (item.itemCategory === ItemCategory.SPARE) totalSpares += itemTotal;
    else if (item.itemCategory === ItemCategory.LABOUR) totalLabour += itemTotal;
    else if (item.itemCategory === ItemCategory.LUBE || item.itemCategory === ItemCategory.DETAILING) totalLubes += itemTotal;
  }

  const subTotal = totalSpares + totalLabour + totalLubes;
  const gstPercent = 18.0;
  const gstAmount = (subTotal * gstPercent) / 100.0;
  const grandTotal = subTotal + gstAmount;
  const balanceAmount = Math.max(0, grandTotal - advancePaid);

  return {
    totalSpares,
    totalLabour,
    totalLubes,
    subTotal,
    gstPercent,
    gstAmount,
    cgstAmount: gstAmount / 2.0,
    sgstAmount: gstAmount / 2.0,
    grandTotal,
    advancePaid,
    balanceAmount
  };
}
```

---

### 4.3 6-Point Dent & Scratch Photo Inspection System

When vehicles are received at the workshop, disputes often arise regarding preexisting dents or scratches. The system solves this with **6 standardized angles**:

```typescript
export interface DentPhotoItem {
  angleIndex: number;      // 0 to 5
  title: string;           // Standard label
  description: string;     // Specific inspection focus
  photoUrl: string;        // Cloud storage URL / Base64 / Local URI
  hasDent: boolean;        // True if marks detected
  severity: "NO_DENT" | "MINOR_SCRATCH" | "MEDIUM_DENT" | "MAJOR_DAMAGE";
  severityLabel: string;   // Clean readable string
  notes: string;           // Inspector remarks (e.g. "2cm paint chip on left fender")
}
```

#### Standard Angle Definitions:
1. **Angle 0: Front View** — Front Bumper, Hood, Grille, Headlamps, Number plate.
2. **Angle 1: Rear View** — Rear Bumper, Tailgate/Boot, Taillights, Exhaust.
3. **Angle 2: Left Side Profile** — Front & Rear Left Doors, Left Fender, Running Board, Left ORVM.
4. **Angle 3: Right Side Profile** — Front & Rear Right Doors, Right Quarter Panel, Right ORVM.
5. **Angle 4: Roof & Glass** — Windshield glass, Roof panel, Sunroof, Rear windshield.
6. **Angle 5: Close-Up Dent/Scratch** — High-resolution macro zoom of the most significant existing scratch or dent.

#### WhatsApp Share Template (Pre-Service Report):
```
📋 *GVD AUTO WORLD BANGALORE*
━━━━━━━━━━━━━━━━━━━━
🔍 *6-POINT VEHICLE DENT & BODY INSPECTION REPORT*
🚗 *Vehicle Number:* KA 01 MJ 5821
👤 *Customer:* Anand Kumar

*CHECK-IN CONDITION LOG:*
1. ⚠️ *1. Front Angle:* Minor Scratch (Minor paint scratch on left front bumper)
2. ✅ *2. Rear Angle:* All Clear (No remarks)
3. ⚠️ *3. Left Side Profile:* Medium Dent (Visible dimple on passenger door)
4. ✅ *4. Right Side Profile:* All Clear (Clean condition)
5. ✅ *5. Roof & Glass:* All Clear (Windshield spotless)
6. ⚠️ *6. Close-Up Dent / Scratch:* Minor Scratch (Close-up captured)

━━━━━━━━━━━━━━━━━━━━
📍 *GVD Auto World Kundalahalli Hub (Whitefield)*
Google Maps: https://maps.app.goo.gl/xgdEGHBKRth1TUrs7
All 6 inspection photos have been digitally verified before service commencement.
```

---

### 4.4 Main Inventory Engine & Excel/CSV Batch Inward

The Admin **Main Inventory** module tracks stock levels, flags low stock (`stockQuantity <= minThresholdAlert`), and imports parts lists from vendors.

#### File Formats Supported:
- Modern Microsoft Excel (`.xlsx` OpenXML zipped XML)
- Legacy Excel (`.xls`)
- Standard Comma-Separated Values (`.csv`)

#### Inward Column Detection Heuristics:
The parser recognizes columns via case-insensitive keyword matches:
- **Part Name:** `part`, `name`, `description`, `item`, `spares`
- **Part Number / SKU:** `sku`, `part number`, `partno`, `code`, `item no`
- **Quantity:** `qty`, `quantity`, `units`, `pcs`, `count`, `received`
- **Unit Price:** `price`, `mrp`, `rate`, `cost`, `unit price`, `amount`
- **Category:** `category`, `type`, `cat` (mapped to `SPARE`, `LUBE`, `DETAILING`)
- **Unit:** `unit`, `uom` (e.g. `pcs`, `can`, `ltr`, `set`)

#### Batch Inward Processing Logic:
```typescript
interface ParsedInventoryItem {
  partName: string;
  partNumber: string;
  category: ItemCategory;
  quantity: number;
  unitPrice: number;
  unit: string;
  supplier: string;
  invoiceNo: string;
}

async function processInventoryBatchInward(
  items: ParsedInventoryItem[],
  supplierName: string,
  invoiceNo: string
) {
  let updatedCount = 0;
  let newCount = 0;

  for (const item of items) {
    const existing = await prisma.inventoryItem.findUnique({
      where: { partNumber: item.partNumber }
    });

    if (existing) {
      await prisma.inventoryItem.update({
        where: { id: existing.id },
        data: {
          stockQuantity: existing.stockQuantity + item.quantity,
          unitPrice: item.unitPrice > 0 ? item.unitPrice : existing.unitPrice
        }
      });
      updatedCount++;
    } else {
      await prisma.inventoryItem.create({
        data: {
          partName: item.partName,
          partNumber: item.partNumber,
          category: item.category,
          compatibleType: VehicleType.TWO_WHEELER,
          unitPrice: item.unitPrice,
          stockQuantity: item.quantity,
          minThresholdAlert: 5,
          unit: item.unit || "pcs"
        }
      });
      newCount++;
    }
  }

  // Record an immutable Purchase Order audit entry
  const totalAmount = items.reduce((sum, i) => sum + i.quantity * i.unitPrice, 0);
  const totalUnits = items.reduce((sum, i) => sum + i.quantity, 0);

  await prisma.purchaseOrder.create({
    data: {
      poNumber: `PO-${Date.now()}`,
      supplierName: supplierName || "Direct Vendor Inward",
      invoiceNumber: invoiceNo || `INV-${Date.now()}`,
      totalAmount,
      totalUnits,
      status: "RECEIVED",
      itemsJson: items as any,
      receivedAt: new Date()
    }
  });

  return { updatedCount, newCount, totalUnits, totalAmount };
}
```

#### Standard CSV Template:
```csv
Part Name,SKU,Category,Quantity,Unit Price,Unit
Bosch Front Ceramic Brake Pads,BSH-BP-402,SPARE,20,950,set
Motul 7100 10W50 4T Fully Synthetic,MTL-OIL-10W50,LUBE,35,1150,can
NGK Laser Iridium Spark Plug,NGK-SP-CR9E,SPARE,25,780,pcs
3M Waterless Car Foam Wash & Wax,3M-CW-500,DETAILING,15,480,bottle
K&N High Flow Performance Air Filter,KN-AF-110,SPARE,10,3200,pcs
Castrol DOT 4 Brake & Clutch Fluid,CAS-BF-DOT4,LUBE,16,280,can
```

---

### 4.5 360° Multi-Point Digital Inspection (Mechanic Mode)

The technician inspects 40 points categorized into systems:
1. **Engine & Transmission:** Engine oil level & viscosity, spark plug wear, coolant level, clutch free play, drive chain slackness & lubrication.
2. **Braking System:** Front brake pad thickness (min 2mm), rear brake shoe/pad, brake disc runout, brake fluid moisture level.
3. **Suspension & Steering:** Front fork oil seals (leak check), rear mono-shock dampening, steering cone bearing play, wheel rim truing.
4. **Electricals & Battery:** Battery terminal voltage (idle vs crank), horn, headlamp high/low beam, indicators, brake tail lamp switch.
5. **Tyres & Chassis:** Front tyre tread depth (TWI indicator), rear tyre tread depth, chassis frame welds, centre/side stand spring tension.

Each item can be set to:
- `GOOD` (Green check)
- `NEED_TO_REPLACE` (Red alert with optional technician note)
- `SERVICED` (Orange wrench)

---

### 4.6 Customer Tracking Portal, Payments & QR Pass

Customers access a dedicated tracking link using their Vehicle Number or Job Card Number (e.g. `https://gvdautoworld.com/track/KA01MJ5821`):
1. **Live Visual Stepper:** Check-in ➜ Diagnostics & Parts ➜ In Service ➜ Quality Check & Wash ➜ Ready for Pickup.
2. **Interactive Estimate Approval:** Review itemized spares & labour before authorizing work.
3. **Online Payment:** Integrated payment gateway (Razorpay / Cashfree / Stripe) supporting UPI (PhonePe, Google Pay, Paytm), Debit/Credit Cards, and Net Banking.
4. **Digital Vehicle Pass:** Generates a secure QR Code encoding:
   - Vehicle Registration Number
   - Job Card ID
   - Customer Name
   - Security verification token for gate exit.
5. **Rating & Reviews:** 1 to 5 star rating with category tags and feedback text, instantly visible in the Admin dashboard.

---

### 4.7 Admin Oversight, Financial Analytics & Automated Reminders

#### Analytics Widgets:
- **Daily Gross Inflow:** Total billings generated today.
- **Monthly Revenue Run-Rate:** Aggregate month-to-date sales.
- **Online vs Counter Split:** Digital payments vs cash pending balance.
- **Bay Productivity:** Total jobs handled, average repair cycle time.
- **Customer Sentiment:** Star distribution (5★, 4★, 3★, 2★, 1★) and response rate.

#### WhatsApp Service Reminder Dispatcher:
Tracks vehicles due for:
- 3-Month / 3,000 KM Periodic Lube Service
- Chain Maintenance & Sprocket Check
- Annual Monsoon Check-Up
- Battery Health Check

Automated URL trigger format:
```
https://api.whatsapp.com/send?phone=91{CUSTOMER_MOBILE}&text=Dear%20{CUSTOMER_NAME},%20your%20vehicle%20{VEHICLE_NO}%20is%20due%20for%20its%20periodic%20maintenance%20at%20GVD%20Auto%20World.%20Book%20here:%20https://maps.app.goo.gl/xgdEGHBKRth1TUrs7
```

---

## 5. REST / GraphQL / tRPC API Specification

Below are the key endpoints to implement in the target web application:

### Job Cards & Invoicing
- `POST /api/job-cards` — Create a new job card (takes vehicle details, odometer, fuel, accessories, and 6 dent photos JSON).
- `GET /api/job-cards` — List all job cards with filtering by status (`CHECKED_IN`, `IN_PROGRESS`, etc.), date range, or vehicle number.
- `GET /api/job-cards/:id` — Full details of a job card including items, dent photos, and payment history.
- `PATCH /api/job-cards/:id/status` — Update stage (`IN_PROGRESS`, `READY_FOR_DELIVERY`, `DELIVERED`).
- `POST /api/job-cards/:id/items` — Add a spare, lubricant, or labour item.
- `DELETE /api/job-cards/:id/items/:itemId` — Remove a line item and recalculate GST.
- `POST /api/job-cards/:id/payments` — Record online or counter payment.

### 6-Point Dent Inspection
- `POST /api/job-cards/:id/dent-photos` — Upload or update photos for the 6 angles with severity and notes.
- `GET /api/job-cards/:id/dent-photos` — Retrieve inspection photos for web viewer / WhatsApp sharing.

### Main Inventory & Inward Upload
- `GET /api/inventory` — List warehouse stock with category and search filter.
- `POST /api/inventory` — Add a single SKU manually.
- `PATCH /api/inventory/:id/stock` — Adjust stock quantity (`+1` or `-1`).
- `POST /api/inventory/upload-excel` — Multipart upload of `.xlsx` or `.csv` file. Returns parsed items, preview, and updates database in a transaction.

### Multi-Point 360° Inspection
- `GET /api/job-cards/:id/inspection` — Get checklist state for the technician.
- `PUT /api/job-cards/:id/inspection/:itemId` — Update item status (`GOOD`, `NEED_TO_REPLACE`, `SERVICED`) with notes and photo proof.

### Customer Public Tracking
- `GET /api/public/track/:vehicleNumber` — Unauthenticated tracking endpoint returning sanitized job status, technician notes, and payment link.
- `POST /api/public/reviews` — Submit a customer rating and feedback.

### Admin Oversight
- `GET /api/admin/metrics` — Aggregate financial metrics (today sales, monthly run rate, pending balances).
- `GET /api/admin/reminders` — Get list of pending service reminders.
- `POST /api/admin/reminders/:id/dispatch` — Mark reminder as sent via WhatsApp/SMS.
- `POST /api/admin/reviews/:id/reply` — Submit official admin reply to customer review.

---

## 6. Target Web Tech Stack & Architecture

To achieve the best developer velocity, mobile-friendly responsiveness, and high performance, the following modern web stack is recommended:

```
[ Frontend: Next.js 14+ (App Router) ]
  ├── TypeScript for strict type-safety
  ├── Tailwind CSS for styling
  ├── shadcn/ui (Radix Primitives) for clean accessible UI components
  ├── Lucide React for consistent icons
  ├── TanStack Table for admin inventory and job card data tables
  └── SheetJS (xlsx) & PapaParse for browser-side Excel/CSV parsing

[ Backend: Next.js Server Actions & API Routes (or Express / NestJS) ]
  ├── Prisma ORM for typed database access
  ├── PostgreSQL (Hosted on Supabase, Neon, or AWS RDS)
  ├── Cloudinary or AWS S3 for storing the 6 dent photos and inspection images
  └── Razorpay SDK for Indian UPI/Card payments

[ Deployment & Hosting ]
  ├── Vercel or AWS ECS/Docker for Web Application
  ├── Supabase or Neon for Managed Serverless Postgres
  └── WhatsApp Cloud API / Twilio for automated message delivery
```

---

## 7. Step-by-Step Migration Plan

When taking this project to your new development environment:

### Phase 1: Project Initialization
1. Initialize a modern Next.js project:
   ```bash
   npx create-next-app@latest gvd-auto-world-web --typescript --tailwind --eslint --app
   cd gvd-auto-world-web
   ```
2. Install dependencies:
   ```bash
   npm install @prisma/client xlsx papaparse lucide-react qrcode.react @tanstack/react-table clsx tailwind-merge
   npm install -D prisma
   ```
3. Initialize Prisma and paste the schema provided in **Section 3**:
   ```bash
   npx prisma init
   npx prisma migrate dev --name init_gvd_schema
   ```

### Phase 2: Core Components Migration
1. **Header & Workshop Banner:** Build a reusable top bar featuring the GVD Auto World brand colors (Crimson Red `#C62828`, Charcoal `#1C1C1E`) and Google Maps Kundalahalli link.
2. **Job Card Wizard:** Build a 3-tab multi-step form with Step 1 (Vehicle & Owner), Step 2 (Odometer & 6-point Dent Grid with camera/file picker), and Step 3 (Spares selection and live 18% GST calculator).
3. **6 Dent Photos Grid:** Implement the 6 thumbnail cards with severity pills and modal image zoom.
4. **Excel Import Modal:** Use SheetJS (`xlsx`) to parse incoming delivery files on the client side, preview detected SKUs, and submit to `/api/inventory/upload-excel`.

### Phase 3: Role Portals & Customer Tracking
1. **Advisor Dashboard:** List of active vehicles, quick intake button, payment status badges.
2. **Technician Bay View:** Mobile-optimized checklist with high-contrast buttons (`GOOD` / `REPLACE` / `SERVICED`).
3. **Customer Public View (`/track/[vehicleNumber]`):** Mobile-first tracking page with live status bar, QR pass generator, and Razorpay checkout button.
4. **Admin Oversight:** Financial KPIs, inventory warehouse table with instant search and low stock badges, automated WhatsApp reminder dispatcher, and review replies.

### Phase 4: Data Seeding & Testing
1. Seed the initial warehouse inventory with OEM spares (brake pads, Motul oils, NGK plugs, 3M detailing products).
2. Seed sample job cards in various states (`CHECKED_IN`, `IN_PROGRESS`, `READY_FOR_DELIVERY`) to verify all UI flows.

---

*Document generated for GVD Auto World Technical Migration. All rights reserved.*
