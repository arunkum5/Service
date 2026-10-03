# GVD Auto World — Comprehensive Web Application Migration Guide & Technical Specification

> **Notice:** This document contains the complete technical architecture, relational database schema, data models, business logic, API specifications, and raw seed data dump to migrate the **GVD Auto World** Android application into a modern full-stack web application (e.g., Next.js / React / TypeScript / PostgreSQL) on another machine.
>
> *No Android source code files have been modified. This file serves as the self-contained migration blueprint.*

---

## Table of Contents
1. [Executive Summary & Workshop Metadata](#1-executive-summary--workshop-metadata)
2. [Target Web Architecture & Tech Stack Recommendation](#2-target-web-architecture--tech-stack-recommendation)
3. [Relational Database Schema & Data Models](#3-relational-database-schema--data-models)
   - [3.1 PostgreSQL DDL (Schema Creation)](#31-postgresql-ddl-schema-creation)
   - [3.2 Prisma Schema (`schema.prisma`)](#32-prisma-schema-schemaprisma)
   - [3.3 TypeScript Data Interfaces](#33-typescript-data-interfaces)
4. [Complete Raw Seed Data Dump](#4-complete-raw-seed-data-dump)
   - [4.1 SQL Insert Scripts](#41-sql-insert-scripts)
   - [4.2 JSON Seed Dump](#42-json-seed-dump)
5. [User Roles & Access Control Matrix (RBAC)](#5-user-roles--access-control-matrix-rbac)
6. [Job Card Lifecycle & State Machine](#6-job-card-lifecycle--state-machine)
7. [Screen-by-Screen Web Functional Specifications](#7-screen-by-screen-web-functional-specifications)
   - [7.1 Customer Portal (`/` & `/customer`)](#71-customer-portal--customer)
   - [7.2 Service Advisor / Staff Dashboard (`/staff`)](#72-service-advisor--staff-dashboard-staff)
   - [7.3 Job Card Creation Wizard with 6-Angle Dent Capture (`/staff/job-cards/new`)](#73-job-card-creation-wizard-with-6-angle-dent-capture-staffjob-cardsnew)
   - [7.4 Job Card Detail & Billing Management (`/staff/job-cards/[id]`)](#74-job-card-detail--billing-management-staffjob-cardsid)
   - [7.5 Technician 360° Inspection Bay (`/technician`)](#75-technician-360-inspection-bay-technician)
   - [7.6 Inventory Management & Excel/CSV Batch Import (`/admin/inventory`)](#76-inventory-management--excelcsv-batch-import-admininventory)
   - [7.7 Purchase Order Generation (`/admin/purchase-orders`)](#77-purchase-order-generation-adminpurchase-orders)
   - [7.8 Vehicle QR Scanner & Pass Generator (`/scanner` & `/pass/[vehicleNumber]`)](#78-vehicle-qr-scanner--pass-generator-scanner--passvehiclenumber)
   - [7.9 Workshop Location & Navigation (`/location`)](#79-workshop-location--navigation-location)
8. [Core Business Logic & Algorithms](#8-core-business-logic--algorithms)
   - [8.1 Invoice & Financial Calculations](#81-invoice--financial-calculations)
   - [8.2 Excel & CSV Parts Import Parser (SheetJS)](#82-excel--csv-parts-import-parser-sheetjs)
   - [8.3 QR Code Payload Structure](#83-qr-code-payload-structure)
   - [8.4 WhatsApp & SMS Notification Templates](#84-whatsapp--sms-notification-templates)
9. [REST API Endpoint Specifications](#9-rest-api-endpoint-specifications)
10. [Step-by-Step Machine Setup & Migration Roadmap](#10-step-by-step-machine-setup--migration-roadmap)

---

## 1. Executive Summary & Workshop Metadata

**GVD Auto World** is an enterprise-grade automotive workshop and vehicle sales/service management platform handling two-wheelers (motorcycles/scooters) and four-wheelers (cars/SUVs).

### Core Workshop Details
- **Workshop Name:** GVD Auto World — Bengaluru Hub
- **Physical Address:** Vibgyor High School Road, GJM Sai Garden Layout, Kundalahalli, Bengaluru, Karnataka 560037
- **GPS Coordinates:** Latitude `12.9551796`, Longitude `77.717493`
- **Google Maps Location:** [https://maps.app.goo.gl/xgdEGHBKRth1TUrs7](https://maps.app.goo.gl/xgdEGHBKRth1TUrs7)
- **Primary Support Line:** `+91 98450 12345` / `+91 86987 61486`
- **Support Email:** `support@gvdautoworld.com`
- **Specializations:** Periodic Mechanical Service, 360° Digital Vehicle Health Inspections, 9H Ceramic Coating, TPU Paint Protection Film (PPF), Collision Repairs, Insurance Claims, Certified Pre-Owned Vehicle Sales.

---

## 2. Target Web Architecture & Tech Stack Recommendation

To achieve high performance, responsiveness across mobile/desktop browsers, and seamless deployment on any machine or cloud provider:

| Layer | Recommended Technology | Alternatives |
|---|---|---|
| **Frontend Framework** | **Next.js 14/15 (App Router)** + React 19 / TypeScript | Vite + React + React Router |
| **Styling & UI Components** | **Tailwind CSS + Shadcn UI** (Radix UI primitives) | Material UI (MUI v5) / Mantine |
| **Icons** | **Lucide React** | Material Symbols / Heroicons |
| **State Management** | **TanStack Query (React Query) + Zustand** | Redux Toolkit |
| **Database** | **PostgreSQL (v15+)** | MySQL / SQLite |
| **ORM / Query Builder** | **Prisma ORM** or **Drizzle ORM** | TypeORM |
| **File / Photo Storage** | **Cloudflare R2** or **AWS S3** / Supabase Storage | Local filesystem / MinIO |
| **Excel / CSV Processing** | **SheetJS (`xlsx`)** + **PapaParse** | ExcelJS |
| **QR Code Engine** | **`html5-qrcode`** (Camera Scanner) + **`qrcode.react`** | ZXing Browser |
| **PDF Generation** | **`@react-pdf/renderer`** or **`jspdf` + `jspdf-autotable`** | Puppeteer server-side |
| **Interactive Maps** | **Leaflet (`react-leaflet`)** or **Google Maps JavaScript API** | Mapbox GL |
| **Deployment** | **Docker Container** or **Vercel / Railway / Render** | AWS EC2 / DigitalOcean |

---

## 3. Relational Database Schema & Data Models

### 3.1 PostgreSQL DDL (Schema Creation)

```sql
-- 1. Custom Enum Types
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'TECHNICIAN', 'STAFF', 'ADMIN');
CREATE TYPE vehicle_type AS ENUM ('TWO_WHEELER', 'FOUR_WHEELER');
CREATE TYPE job_card_status AS ENUM ('OPEN', 'IN_PROGRESS', 'QUALITY_CHECK', 'READY', 'COMPLETED', 'CLOSED');
CREATE TYPE component_status AS ENUM ('GOOD', 'SERVICED', 'NEED_REPLACE');
CREATE TYPE item_category AS ENUM ('SPARE', 'LABOUR', 'LUBE', 'DETAILING');
CREATE TYPE inspection_angle AS ENUM ('FRONT', 'SIDE_LEFT', 'REAR', 'SIDE_RIGHT', 'ENGINE_BAY', 'INTERIOR', 'UNDERBODY');

-- 2. Job Cards Table
CREATE TABLE job_cards (
    id BIGSERIAL PRIMARY KEY,
    job_card_number VARCHAR(50) UNIQUE NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    customer_mobile VARCHAR(20) NOT NULL,
    customer_email VARCHAR(150) DEFAULT '',
    vehicle_number VARCHAR(30) NOT NULL,
    vehicle_type vehicle_type NOT NULL DEFAULT 'TWO_WHEELER',
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    variant VARCHAR(100) DEFAULT '',
    odometer_km INTEGER DEFAULT 0,
    fuel_level_percent INTEGER DEFAULT 50,
    accessories_notes TEXT DEFAULT '',
    customer_voice TEXT DEFAULT '',
    dent_notes TEXT DEFAULT '',
    dent_photos_json JSONB DEFAULT '[]'::jsonb,
    status job_card_status NOT NULL DEFAULT 'OPEN',
    total_spares NUMERIC(12,2) DEFAULT 0.00,
    total_labour NUMERIC(12,2) DEFAULT 0.00,
    total_lubes NUMERIC(12,2) DEFAULT 0.00,
    total_amount NUMERIC(12,2) DEFAULT 0.00,
    advance_paid NUMERIC(12,2) DEFAULT 0.00,
    balance_amount NUMERIC(12,2) DEFAULT 0.00,
    delivery_date_time VARCHAR(50) DEFAULT '',
    is_sms_alert_enabled BOOLEAN DEFAULT true,
    is_paid_online BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_job_cards_vehicle ON job_cards(vehicle_number);
CREATE INDEX idx_job_cards_mobile ON job_cards(customer_mobile);
CREATE INDEX idx_job_cards_status ON job_cards(status);

-- 3. Job Card Items Table (Spares, Labour, Lubes, Detailing)
CREATE TABLE job_card_items (
    id BIGSERIAL PRIMARY KEY,
    job_card_id BIGINT NOT NULL REFERENCES job_cards(id) ON DELETE CASCADE,
    category item_category NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER DEFAULT 1,
    unit_price NUMERIC(12,2) NOT NULL,
    discount NUMERIC(12,2) DEFAULT 0.00,
    total_amount NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_job_card_items_jc ON job_card_items(job_card_id);

-- 4. 360 Component Inspections Table
CREATE TABLE component_inspections (
    id BIGSERIAL PRIMARY KEY,
    vehicle_number VARCHAR(30) NOT NULL,
    job_card_id BIGINT DEFAULT 0,
    component_key VARCHAR(100) NOT NULL,
    component_name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    angle VARCHAR(50) NOT NULL,
    status component_status NOT NULL DEFAULT 'GOOD',
    technician_notes TEXT DEFAULT '',
    works_till_info VARCHAR(255) DEFAULT 'Works till next service (5,000 km)',
    recommended_action TEXT DEFAULT '',
    replacement_cost NUMERIC(12,2) DEFAULT 0.00,
    work_photo_url TEXT DEFAULT '',
    last_serviced_date VARCHAR(50) DEFAULT '2026-06-15',
    updated_timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_component_inspections_veh ON component_inspections(vehicle_number);

-- 5. Master Inventory Items Table
CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    part_name VARCHAR(255) NOT NULL,
    part_number VARCHAR(100) UNIQUE NOT NULL,
    category item_category NOT NULL,
    compatible_type vehicle_type NOT NULL DEFAULT 'TWO_WHEELER',
    unit_price NUMERIC(12,2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    min_threshold_alert INTEGER NOT NULL DEFAULT 5,
    unit VARCHAR(20) DEFAULT 'pcs',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_category ON inventory_items(category);
CREATE INDEX idx_inventory_part_no ON inventory_items(part_number);

-- 6. Service Appointments Table
CREATE TABLE service_appointments (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(150) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    vehicle_number VARCHAR(30) NOT NULL,
    vehicle_type vehicle_type NOT NULL,
    service_package VARCHAR(150) NOT NULL,
    preferred_date VARCHAR(50) NOT NULL,
    preferred_slot VARCHAR(50) NOT NULL,
    is_doorstep_pickup BOOLEAN DEFAULT false,
    customer_voice TEXT DEFAULT '',
    status VARCHAR(50) DEFAULT 'CONFIRMED',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 7. Showroom Vehicle Inventory Table (Pre-Owned / Certified)
CREATE TABLE vehicle_showroom_inventory (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    vehicle_type vehicle_type NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    km_driven INTEGER NOT NULL,
    fuel_type VARCHAR(50) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    emi_starting_at NUMERIC(12,2) NOT NULL,
    specs_summary TEXT NOT NULL,
    condition VARCHAR(100) DEFAULT 'Certified Pre-Owned',
    warranty_months INTEGER DEFAULT 12,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 8. Service Reminders Table
CREATE TABLE service_reminders (
    id BIGSERIAL PRIMARY KEY,
    vehicle_number VARCHAR(30) NOT NULL,
    customer_name VARCHAR(150) NOT NULL,
    customer_mobile VARCHAR(20) NOT NULL,
    reminder_type VARCHAR(100) NOT NULL,
    due_date VARCHAR(50) NOT NULL,
    message_text TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    is_whatsapp_triggered BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 9. Customer Reviews & Ratings Table
CREATE TABLE customer_reviews (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(150) NOT NULL,
    customer_mobile VARCHAR(20) DEFAULT '',
    vehicle_number VARCHAR(30) DEFAULT '',
    vehicle_model VARCHAR(100) DEFAULT '',
    service_type VARCHAR(100) DEFAULT 'Periodic Service',
    rating INTEGER CHECK (rating >= 1 AND rating <= 5) DEFAULT 5,
    aspect_punctuality INTEGER CHECK (aspect_punctuality >= 1 AND aspect_punctuality <= 5) DEFAULT 5,
    aspect_cleanliness INTEGER CHECK (aspect_cleanliness >= 1 AND aspect_cleanliness <= 5) DEFAULT 5,
    aspect_pricing INTEGER CHECK (aspect_pricing >= 1 AND aspect_pricing <= 5) DEFAULT 5,
    tags TEXT DEFAULT '',
    comment TEXT DEFAULT '',
    job_card_number VARCHAR(50) DEFAULT '',
    admin_response TEXT DEFAULT '',
    admin_responded_at BIGINT DEFAULT 0,
    is_verified_client BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 10. Purchase Orders Table (Supplier Inward Orders & Batch Imports)
CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(100) UNIQUE NOT NULL,
    supplier_name VARCHAR(200) DEFAULT 'Bangalore OEM Spares & Lubricants Wholesale Hub',
    supplier_address VARCHAR(255) DEFAULT 'JC Road, Kalasipalya, Bangalore 560002',
    supplier_contact VARCHAR(50) DEFAULT '+91 98450 12345',
    status VARCHAR(50) DEFAULT 'PENDING',
    total_items_count INTEGER DEFAULT 0,
    total_units_count INTEGER DEFAULT 0,
    subtotal NUMERIC(12,2) DEFAULT 0.00,
    gst_amount NUMERIC(12,2) DEFAULT 0.00,
    grand_total NUMERIC(12,2) DEFAULT 0.00,
    notes TEXT DEFAULT '',
    items_json JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    received_at BIGINT DEFAULT 0
);
```

---

### 3.2 Prisma Schema (`schema.prisma`)

```prisma
datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

generator client {
  provider = "prisma-client-js"
}

enum UserRole {
  CUSTOMER
  TECHNICIAN
  STAFF
  ADMIN
}

enum VehicleType {
  TWO_WHEELER
  FOUR_WHEELER
}

enum JobCardStatus {
  OPEN
  IN_PROGRESS
  QUALITY_CHECK
  READY
  COMPLETED
  CLOSED
}

enum ComponentStatus {
  GOOD
  SERVICED
  NEED_REPLACE
}

enum ItemCategory {
  SPARE
  LABOUR
  LUBE
  DETAILING
}

model JobCard {
  id                BigInt         @id @default(autoincrement())
  jobCardNumber     String         @unique @map("job_card_number")
  customerName      String         @map("customer_name")
  customerMobile    String         @map("customer_mobile")
  customerEmail     String?        @default("") @map("customer_email")
  vehicleNumber     String         @map("vehicle_number")
  vehicleType       VehicleType    @default(TWO_WHEELER) @map("vehicle_type")
  make              String
  model             String
  variant           String?        @default("")
  odometerKm        Int            @default(0) @map("odometer_km")
  fuelLevelPercent  Int            @default(50) @map("fuel_level_percent")
  accessoriesNotes  String?        @default("") @map("accessories_notes")
  customerVoice     String?        @default("") @map("customer_voice")
  dentNotes         String?        @default("") @map("dent_notes")
  dentPhotosJson    Json?          @default("[]") @map("dent_photos_json")
  status            JobCardStatus  @default(OPEN)
  totalSpares       Decimal        @default(0.00) @map("total_spares") @db.Decimal(12, 2)
  totalLabour       Decimal        @default(0.00) @map("total_labour") @db.Decimal(12, 2)
  totalLubes        Decimal        @default(0.00) @map("total_lubes") @db.Decimal(12, 2)
  totalAmount       Decimal        @default(0.00) @map("total_amount") @db.Decimal(12, 2)
  advancePaid       Decimal        @default(0.00) @map("advance_paid") @db.Decimal(12, 2)
  balanceAmount     Decimal        @default(0.00) @map("balance_amount") @db.Decimal(12, 2)
  deliveryDateTime  String?        @default("") @map("delivery_date_time")
  isSmsAlertEnabled Boolean        @default(true) @map("is_sms_alert_enabled")
  isPaidOnline      Boolean        @default(false) @map("is_paid_online")
  createdAt         DateTime       @default(now()) @map("created_at")
  updatedAt         DateTime       @updatedAt @map("updated_at")
  items             JobCardItem[]

  @@map("job_cards")
  @@index([vehicleNumber])
  @@index([customerMobile])
}

model JobCardItem {
  id          BigInt       @id @default(autoincrement())
  jobCardId   BigInt       @map("job_card_id")
  jobCard     JobCard      @relation(fields: [jobCardId], references: [id], onDelete: Cascade)
  category    ItemCategory
  name        String
  quantity    Int          @default(1)
  unitPrice   Decimal      @map("unit_price") @db.Decimal(12, 2)
  discount    Decimal      @default(0.00) @db.Decimal(12, 2)
  totalAmount Decimal      @map("total_amount") @db.Decimal(12, 2)
  createdAt   DateTime     @default(now()) @map("created_at")

  @@map("job_card_items")
}

model ComponentInspection {
  id                BigInt          @id @default(autoincrement())
  vehicleNumber     String          @map("vehicle_number")
  jobCardId         BigInt          @default(0) @map("job_card_id")
  componentKey      String          @map("component_key")
  componentName     String          @map("component_name")
  category          String
  angle             String
  status            ComponentStatus @default(GOOD)
  technicianNotes   String?         @default("") @map("technician_notes")
  worksTillInfo     String?         @default("Works till next service (5,000 km)") @map("works_till_info")
  recommendedAction String?         @default("") @map("recommended_action")
  replacementCost   Decimal         @default(0.00) @map("replacement_cost") @db.Decimal(12, 2)
  workPhotoUrl      String?         @default("") @map("work_photo_url")
  lastServicedDate  String?         @default("2026-06-15") @map("last_serviced_date")
  updatedTimestamp  DateTime        @default(now()) @map("updated_timestamp")

  @@map("component_inspections")
  @@index([vehicleNumber])
}

model InventoryItem {
  id                 BigInt       @id @default(autoincrement())
  partName           String       @map("part_name")
  partNumber         String       @unique @map("part_number")
  category           ItemCategory
  compatibleType     VehicleType  @default(TWO_WHEELER) @map("compatible_type")
  unitPrice          Decimal      @map("unit_price") @db.Decimal(12, 2)
  stockQuantity      Int          @default(0) @map("stock_quantity")
  minThresholdAlert  Int          @default(5) @map("min_threshold_alert")
  unit               String       @default("pcs")
  createdAt          DateTime     @default(now()) @map("created_at")
  updatedAt          DateTime     @updatedAt @map("updated_at")

  @@map("inventory_items")
}

model ServiceAppointment {
  id               BigInt      @id @default(autoincrement())
  customerName     String      @map("customer_name")
  customerPhone    String      @map("customer_phone")
  vehicleNumber    String      @map("vehicle_number")
  vehicleType      VehicleType @map("vehicle_type")
  servicePackage   String      @map("service_package")
  preferredDate    String      @map("preferred_date")
  preferredSlot    String      @map("preferred_slot")
  isDoorstepPickup Boolean     @default(false) @map("is_doorstep_pickup")
  customerVoice    String?     @default("") @map("customer_voice")
  status           String      @default("CONFIRMED")
  createdAt        DateTime    @default(now()) @map("created_at")

  @@map("service_appointments")
}

model VehicleShowroomInventory {
  id             BigInt      @id @default(autoincrement())
  title          String
  vehicleType    VehicleType @map("vehicle_type")
  brand          String
  model          String
  year           Int
  kmDriven       Int         @map("km_driven")
  fuelType       String      @map("fuel_type")
  price          Decimal     @db.Decimal(12, 2)
  emiStartingAt  Decimal     @map("emi_starting_at") @db.Decimal(12, 2)
  specsSummary   String      @map("specs_summary")
  condition      String      @default("Certified Pre-Owned")
  warrantyMonths Int         @default(12) @map("warranty_months")
  isAvailable    Boolean     @default(true) @map("is_available")
  createdAt      DateTime    @default(now()) @map("created_at")

  @@map("vehicle_showroom_inventory")
}

model CustomerReview {
  id                 BigInt   @id @default(autoincrement())
  customerName       String   @map("customer_name")
  customerMobile     String?  @default("") @map("customer_mobile")
  vehicleNumber      String?  @default("") @map("vehicle_number")
  vehicleModel       String?  @default("") @map("vehicle_model")
  serviceType        String   @default("Periodic Service") @map("service_type")
  rating             Int      @default(5)
  aspectPunctuality  Int      @default(5) @map("aspect_punctuality")
  aspectCleanliness  Int      @default(5) @map("aspect_cleanliness")
  aspectPricing      Int      @default(5) @map("aspect_pricing")
  tags               String?  @default("")
  comment            String?  @default("")
  jobCardNumber      String?  @default("") @map("job_card_number")
  adminResponse      String?  @default("") @map("admin_response")
  adminRespondedAt   BigInt?  @default(0) @map("admin_responded_at")
  isVerifiedClient   Boolean  @default(true) @map("is_verified_client")
  createdAt          DateTime @default(now()) @map("created_at")

  @@map("customer_reviews")
}

model PurchaseOrder {
  id               BigInt   @id @default(autoincrement())
  poNumber         String   @unique @map("po_number")
  supplierName     String   @default("Bangalore OEM Spares Wholesale Hub") @map("supplier_name")
  supplierAddress  String   @default("JC Road, Kalasipalya, Bangalore 560002") @map("supplier_address")
  supplierContact  String   @default("+91 98450 12345") @map("supplier_contact")
  status           String   @default("PENDING")
  totalItemsCount  Int      @default(0) @map("total_items_count")
  totalUnitsCount  Int      @default(0) @map("total_units_count")
  subtotal         Decimal  @default(0.00) @db.Decimal(12, 2)
  gstAmount        Decimal  @default(0.00) @map("gst_amount") @db.Decimal(12, 2)
  grandTotal       Decimal  @default(0.00) @map("grand_total") @db.Decimal(12, 2)
  notes            String?  @default("")
  itemsJson        Json     @default("[]") @map("items_json")
  createdAt        DateTime @default(now()) @map("created_at")
  receivedAt       BigInt?  @default(0) @map("received_at")

  @@map("purchase_orders")
}
```

---

### 3.3 TypeScript Data Interfaces

```typescript
export type UserRole = 'CUSTOMER' | 'TECHNICIAN' | 'STAFF' | 'ADMIN';
export type VehicleType = 'TWO_WHEELER' | 'FOUR_WHEELER';
export type JobCardStatus = 'OPEN' | 'IN_PROGRESS' | 'QUALITY_CHECK' | 'READY' | 'COMPLETED' | 'CLOSED';
export type ComponentStatus = 'GOOD' | 'SERVICED' | 'NEED_REPLACE';
export type ItemCategory = 'SPARE' | 'LABOUR' | 'LUBE' | 'DETAILING';

export interface DentPhotoItem {
  angleIndex: number; // 0 to 5
  angleKey: 'FRONT' | 'REAR' | 'LEFT_SIDE' | 'RIGHT_SIDE' | 'ROOF_GLASS' | 'CLOSEUP_DENT';
  title: string;
  description: string;
  photoUri: string;
  hasDent: boolean;
  severity: 'NO_DENT' | 'MINOR_SCRATCH' | 'MEDIUM_DENT' | 'MAJOR_DAMAGE';
  notes: string;
}

export interface PurchaseOrderItem {
  inventoryId: number;
  partName: string;
  partNumber: string;
  category: string;
  currentStock: number;
  minThreshold: number;
  orderQuantity: number;
  unitPrice: number;
  lineTotal: number;
}
```

---

## 4. Complete Raw Seed Data Dump

### 4.1 SQL Insert Scripts

```sql
-- 1. Master Inventory Seed
INSERT INTO inventory_items (part_name, part_number, category, compatible_type, unit_price, stock_quantity, min_threshold_alert, unit) VALUES
('AIR FILTER BIG (Bajaj/Universal)', 'SP-BAJ-AF01', 'SPARE', 'TWO_WHEELER', 205.00, 32, 10, 'pcs'),
('Front Disc Brake Pads (Ceramic)', 'SP-BRK-092', 'SPARE', 'TWO_WHEELER', 420.00, 14, 5, 'set'),
('Tata Nexon OEM Brake Pad Set', 'SP-TAT-BP44', 'SPARE', 'FOUR_WHEELER', 2400.00, 8, 4, 'set'),
('Activated Carbon AC Cabin Filter', 'SP-AC-CF12', 'SPARE', 'FOUR_WHEELER', 800.00, 18, 5, 'pcs'),
('Motul 7100 10W50 100% Synthetic 1L', 'LB-MOT-10W50', 'LUBE', 'TWO_WHEELER', 450.00, 45, 15, 'can'),
('Castrol Magnatec 5W30 Full Synth 3.5L', 'LB-CAS-5W30', 'LUBE', 'FOUR_WHEELER', 1850.00, 12, 4, 'can'),
('TPU Paint Protection Film (Roll 15m)', 'DT-PPF-TPU15', 'DETAILING', 'FOUR_WHEELER', 18500.00, 3, 2, 'roll'),
('9H Diamond Ceramic Coating Kit (50ml)', 'DT-CRM-9H', 'DETAILING', 'FOUR_WHEELER', 3500.00, 7, 3, 'box'),
('Chain Lube & Cleaner Combo Pack', 'LB-CHN-CMB', 'LUBE', 'TWO_WHEELER', 380.00, 22, 6, 'pack'),
('NGK Iridium Spark Plug CR8EIX', 'SP-NGK-CR8', 'SPARE', 'TWO_WHEELER', 650.00, 2, 5, 'pcs'); -- Low stock trigger

-- 2. Seed Job Card 1 (Two-Wheeler - Bajaj Avenger 150 Street)
INSERT INTO job_cards (
    id, job_card_number, customer_name, customer_mobile, customer_email,
    vehicle_number, vehicle_type, make, model, variant, odometer_km, fuel_level_percent,
    accessories_notes, customer_voice, dent_notes, dent_photos_json, status,
    total_spares, total_labour, total_lubes, total_amount, advance_paid, balance_amount,
    delivery_date_time, is_sms_alert_enabled, is_paid_online
) VALUES (
    1, 'JC-2026-0427', 'Ashay Kohad', '8698761486', 'tightthenut@gmail.com',
    'MH12RY1234', 'TWO_WHEELER', 'Bajaj', 'Bajaj Avenger', '150 Street', 25625, 51,
    'Luggage carrier, Crash guard installed',
    'Chain noise on deceleration, front brake squeal, minor vibration above 60 km/h',
    'Minor scratch on left silencer shield, front fender clear',
    '[
      {"angleIndex": 0, "angleKey": "FRONT", "title": "1. Front Angle", "description": "Front Bumper, Hood, Grille", "photoUri": "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800", "hasDent": true, "severity": "MINOR_SCRATCH", "notes": "Hairline stone-chip scratches on lower bumper lip"},
      {"angleIndex": 1, "angleKey": "REAR", "title": "2. Rear Angle", "description": "Tailgate, Bumper & Exhaust", "photoUri": "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800", "hasDent": false, "severity": "NO_DENT", "notes": "Rear silencer and tail pristine"},
      {"angleIndex": 2, "angleKey": "LEFT_SIDE", "title": "3. Left Side Profile", "description": "Doors & Fenders", "photoUri": "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800", "hasDent": true, "severity": "MEDIUM_DENT", "notes": "1.5 inch dent on left silencer shield"},
      {"angleIndex": 3, "angleKey": "RIGHT_SIDE", "title": "4. Right Side Profile", "description": "Right Panels & Skirts", "photoUri": "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800", "hasDent": false, "severity": "NO_DENT", "notes": "No scratches"},
      {"angleIndex": 4, "angleKey": "ROOF_GLASS", "title": "5. Roof & Glass", "description": "Visor & Mirrors", "photoUri": "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800", "hasDent": false, "severity": "NO_DENT", "notes": "Visor intact"},
      {"angleIndex": 5, "angleKey": "CLOSEUP_DENT", "title": "6. Dent / Scratch Close-Up", "description": "Detailed Macro Zoom", "photoUri": "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?w=800", "hasDent": true, "severity": "MINOR_SCRATCH", "notes": "Left silencer heat guard scratch"}
    ]'::jsonb,
    'IN_PROGRESS', 205.00, 800.00, 450.00, 1455.00, 500.00, 955.00,
    '2026-09-24 17:30', true, false
);

INSERT INTO job_card_items (job_card_id, category, name, quantity, unit_price, discount, total_amount) VALUES
(1, 'SPARE', 'AIR FILTER BIG', 1, 205.00, 0.00, 205.00),
(1, 'LABOUR', 'SERVICING MAJOR (2W)', 1, 800.00, 0.00, 800.00),
(1, 'LUBE', 'MOTUL 7100 10W50 4T (1L)', 1, 450.00, 0.00, 450.00);

-- 3. Seed Job Card 2 (Four-Wheeler - Tata Nexon Dark Edition)
INSERT INTO job_cards (
    id, job_card_number, customer_name, customer_mobile, customer_email,
    vehicle_number, vehicle_type, make, model, variant, odometer_km, fuel_level_percent,
    accessories_notes, customer_voice, dent_notes, dent_photos_json, status,
    total_spares, total_labour, total_lubes, total_amount, advance_paid, balance_amount,
    delivery_date_time, is_sms_alert_enabled, is_paid_online
) VALUES (
    2, 'JC-2026-0812', 'Vikram Deshmukh', '9822019942', 'vikram.deshmukh@gmail.com',
    'MH12DY7698', 'FOUR_WHEELER', 'Tata', 'Tata Nexon', 'XZ+ Dark Edition', 21400, 65,
    'Dashcam front/rear, 7D Floor mats',
    'Need 9H Ceramic coating maintenance, AC filter replacement, Brake pads inspection',
    'Slight swirl marks on bonnet, right door small stone chip',
    '[]'::jsonb,
    'OPEN', 3200.00, 7500.00, 1800.00, 12500.00, 3000.00, 9500.00,
    '2026-09-25 18:00', true, false
);

INSERT INTO job_card_items (job_card_id, category, name, quantity, unit_price, discount, total_amount) VALUES
(2, 'DETAILING', '9H Ceramic Coating Annual Re-Coat', 1, 5500.00, 0.00, 5500.00),
(2, 'SPARE', 'Front Brake Pad Set (OEM)', 1, 2400.00, 0.00, 2400.00),
(2, 'SPARE', 'Activated Carbon AC Cabin Filter', 1, 800.00, 0.00, 800.00),
(2, 'LABOUR', 'AC Evaporator Foam Deep Clean', 1, 2000.00, 0.00, 2000.00),
(2, 'LUBE', 'DOT 4 High Temp Brake Fluid Flush', 1, 1800.00, 0.00, 1800.00);

-- 4. Seed 360° Health Component Inspections for MH12RY1234
INSERT INTO component_inspections (vehicle_number, job_card_id, component_key, component_name, category, angle, status, technician_notes, works_till_info, recommended_action, replacement_cost, last_serviced_date) VALUES
('MH12RY1234', 1, 'ENGINE_OIL', 'Engine Oil & Sump Filter', 'Fluid & Lubrication', 'ENGINE_BAY', 'SERVICED', 'Flushed with engine flush. Refilled Motul 7100 10W50.', 'Works till next service (5,000 km)', 'Check level periodically', 450.00, '2026-09-23'),
('MH12RY1234', 1, 'BRAKE_PADS', 'Front & Rear Brakes (Pads & Fluid)', 'Braking & Safety', 'SIDE_LEFT', 'NEED_REPLACE', 'Front pads worn down to 1.8mm (min 2.0mm). Rotor scored.', 'Replace immediately (Safe for ~200 km city only)', 'Replace with Ceramic Disc Pads & DOT 4', 420.00, '2026-03-10'),
('MH12RY1234', 1, 'TIRES', 'Tires, Alignment & Pressure', 'Wheels & Suspension', 'FRONT', 'GOOD', 'Tread depth 5.2mm. Pressure 32 PSI cold.', 'Works till next service (15,000 km remaining)', 'Rotate tires after 8,000 km', 0.00, '2026-09-23'),
('MH12RY1234', 1, 'BATTERY', 'Battery & Charging System', 'Electrical', 'ENGINE_BAY', 'GOOD', 'Resting 12.6V, Cranking 10.4V. Charging 14.2V.', 'Health 92%. Works till next service', 'Keep terminals clean', 0.00, '2026-09-23'),
('MH12RY1234', 1, 'BODY_COATING', 'Ceramic Glaze & Tank PPF', 'Exterior Detailing', 'SIDE_RIGHT', 'SERVICED', 'Hydrophobic ceramic top coat refreshed.', 'Works till next booster (6 months)', 'Use pH-neutral wash shampoo only', 1200.00, '2026-09-23'),
('MH12RY1234', 1, 'CABIN_AC', 'Air Intake & Throttle Body', 'Fuel & Air Delivery', 'INTERIOR', 'SERVICED', 'Throttle body de-carbonized, idle RPM calibrated to 1400.', 'Works till next service (10,000 km)', 'Clean air filter regularly', 800.00, '2026-09-23'),
('MH12RY1234', 1, 'SUSPENSION', 'Shock Absorbers & Bushings', 'Suspension & Handling', 'UNDERBODY', 'GOOD', 'No oil weeping on front fork seals / rear dampers.', 'Works till next service', 'Inspect during next monsoon service', 0.00, '2026-09-23'),
('MH12RY1234', 1, 'EXHAUST', 'Exhaust Pipe & Catalytic Converter', 'Exhaust & Emissions', 'REAR', 'GOOD', 'Emission levels within BS6 norms. Heat shield torqued.', 'Works till next service', 'PUC certificate valid till 2027', 0.00, '2026-09-23');

-- 5. Seed Showroom Certified Pre-Owned Vehicles
INSERT INTO vehicle_showroom_inventory (title, vehicle_type, brand, model, year, km_driven, fuel_type, price, emi_starting_at, specs_summary, condition, warranty_months, is_available) VALUES
('Royal Enfield Hunter 350 Dapper Ash', 'TWO_WHEELER', 'Royal Enfield', 'Hunter 350', 2024, 4800, 'Petrol', 158000.00, 3999.00, '349cc Single Cylinder, Dual-channel ABS, GVD 50-Point Certified, Ceramic Coated', 'Certified Pre-Owned', 12, true),
('KTM Duke 390 Gen 3 Electronic Orange', 'TWO_WHEELER', 'KTM', 'Duke 390', 2024, 3200, 'Petrol', 285000.00, 6499.00, '399cc Liquid Cooled, Cornering ABS, Quickshifter+, 1st Owner, Showroom Condition', 'Certified Pre-Owned', 12, true),
('Tata Nexon XZ+ (S) Dark Edition', 'FOUR_WHEELER', 'Tata', 'Nexon', 2023, 19200, 'Diesel', 1145000.00, 18450.00, '1.5L Revotorq Diesel, Sunroof, Harman Sound, Full Body TPU PPF Installed, 5-Star Safety', 'Certified Pre-Owned', 12, true),
('Hyundai Creta SX (O) Turbo Knight', 'FOUR_WHEELER', 'Hyundai', 'Creta', 2024, 11000, 'Petrol', 1620000.00, 24900.00, '1.5L Turbo GDi 7-speed DCT, Panoramic Sunroof, ADAS Level 2, GVD Auto Warranty', 'Certified Pre-Owned', 12, true),
('Yamaha Aerox 155 MotoGP Edition', 'TWO_WHEELER', 'Yamaha', 'Aerox 155', 2024, 2600, 'Petrol', 129000.00, 3450.00, '155cc VVA Liquid Cooled, Traction Control, Keyless Ignition, Pristine Condition', 'Certified Pre-Owned', 12, true);

-- 6. Seed Customer Reviews with Verified Badges & Admin Responses
INSERT INTO customer_reviews (customer_name, customer_mobile, vehicle_number, vehicle_model, service_type, rating, aspect_punctuality, aspect_cleanliness, aspect_pricing, tags, comment, job_card_number, admin_response, admin_responded_at, is_verified_client) VALUES
('Shashi G', '9845112233', 'KA01MJ9821', 'Royal Enfield Classic 350', 'Periodic Lube & Brake Service', 5, 5, 5, 5, 'Transparent Pricing, WhatsApp Alerts, Genuine Spares', 'Superb service experience at GVD Auto World Kundalahalli! The 360-degree digital inspection report sent to my WhatsApp showed exactly which brake pads needed replacement. Completely transparent billing.', 'JC-2026-0427', 'Thank you Mr. Shashi! Delighted that our digital 360 inspection provided total clarity. Looking forward to serving your Classic 350 again!', 1727800000000, true),
('Rahul Menon', '9822019942', 'KA03HA4321', 'Hyundai Creta SX(O)', '9H Ceramic Coating & Detailing', 5, 5, 5, 4, 'Ceramic Coating, Doorstep Pickup, Flawless Shine', 'Got full body 9H ceramic coating and interior detailing done. The paint depth and water beading are unbelievable! The doorstep pickup from Whitefield was punctual.', 'JC-2026-0812', 'Thank you Rahul! Our detailing masters love working on the Creta. Enjoy the unmatched ceramic gloss on Bangalore roads.', 1727850000000, true),
('Priya Sharma', '9766554433', 'KA05EB7712', 'Honda Activa 6G', 'General Service & Oil Change', 4, 4, 5, 5, 'Courteous Staff, Quality Work, Clean Lounge', 'Very prompt service and clean air-conditioned customer lounge. Staff was courteous and explained the labour invoice line-by-line. Took about 20 mins longer because of rush, but bike feels brand new.', 'JC-2026-0904', 'Thanks for your kind words Priya! We appreciate your patience during our morning rush and are thrilled your Activa is riding smoothly.', 1727880000000, true);

-- 7. Seed Service Appointments
INSERT INTO service_appointments (customer_name, customer_phone, vehicle_number, vehicle_type, service_package, preferred_date, preferred_slot, is_doorstep_pickup, customer_voice, status) VALUES
('Rohan Shinde', '9823456781', 'MH14JB8899', 'FOUR_WHEELER', 'PPF & Ceramic Detailing', '2026-10-10', '10:00 AM', true, 'Need self-healing TPU PPF on bumper, bonnet, and mirrors', 'CONFIRMED'),
('Amit Patil', '9766554433', 'MH12KL3321', 'TWO_WHEELER', 'Bike Service & Chain Overhaul', '2026-10-11', '02:00 PM', false, 'General 10,000 km periodic service', 'CONFIRMED');

-- 8. Seed Service Reminders
INSERT INTO service_reminders (vehicle_number, customer_name, customer_mobile, reminder_type, due_date, message_text, status, is_whatsapp_triggered) VALUES
('MH12RY1234', 'Ashay Kohad', '8698761486', 'Periodic Service', '2026-10-15', 'Dear Ashay, your Bajaj Avenger (MH12RY1234) is due for periodic lube service in 20 days. Book online with GVD Auto World to avail 10% discount.', 'SCHEDULED', false),
('MH12DY7698', 'Vikram Deshmukh', '9822019942', 'Ceramic Coating Booster', '2026-10-01', 'Hi Vikram, your Tata Nexon''s 9H Ceramic hydrophobic booster is due on 1st Oct. Keeps gloss and swirl-protection intact.', 'SCHEDULED', false);
```

---

## 5. User Roles & Access Control Matrix (RBAC)

The system supports four distinct operational personas:

| Capability / Feature | Customer | Technician | Staff (Service Advisor) | Admin / Owner |
|---|:---:|:---:|:---:|:---:|
| **Track Vehicle Status** | Yes (own vehicle) | Yes | Yes (all) | Yes (all) |
| **Interactive 360° Health Report** | View only | Edit / Update status | View | Full access |
| **Create New Job Card** | Request / Book | No | **Full 3-Step Wizard** | Full access |
| **6-Angle Dent Photo Inspection** | View summary | View / Add notes | **Capture & Upload** | Full access |
| **Inventory Picking (Spares/Lube)** | View billed | View stock | Add to Job Card | Full access |
| **Update Job Status** | No | Up to Quality Check | Up to Completed | Full Lifecycle |
| **Payment Collection & Invoice** | Pay online / view | No | Update payment | Full Financials |
| **Excel/CSV Parts Batch Import** | No | No | No | **Yes (SheetJS)** |
| **Create Purchase Orders (PO)** | No | No | View | **Create & Send** |
| **Review Moderation & Reply** | Submit review | No | No | **Publish & Reply** |
| **Showroom Inventory Management** | Inquire / Book | No | View | **Add / Edit / Price** |
| **QR Code Pass Scan** | View QR pass | Scan to inspect | Scan to service | Full scan |

---

## 6. Job Card Lifecycle & State Machine

```
[Customer Inward] 
       │
       ▼
   ┌───────┐
   │  OPEN │ ─── (Service Advisor creates Job Card, 6 Dent Photos, fuel & km recorded)
   └───┬───┘
       │ [Technician assigned, parts picked from inventory]
       ▼
┌──────────────┐
│ IN_PROGRESS  │ ─── (360° Component Health inspection, mechanical work, fluid flush)
└──────┬───────┘
       │ [Work finished, bay supervisor checks vehicle]
       ▼
┌───────────────┐
│ QUALITY_CHECK │ ─── (Test ride, detailing wash, checklist verification)
└──────┬────────┘
       │ [Supervisor signs off, customer notified via WhatsApp/SMS]
       ▼
┌───────────┐
│   READY   │ ─── (Vehicle parked in delivery bay, invoice generated)
└──────┬────┘
       │ [Customer reviews bill, balance settled online or cash/UPI]
       ▼
┌───────────┐
│ COMPLETED │ ─── (Gate pass issued, customer drive away)
└──────┬────┘
       │ [7-day feedback recorded, warranty active]
       ▼
┌───────────┐
│   CLOSED  │ ─── (Archived in historical ledger)
└───────────┘
```

---

## 7. Screen-by-Screen Web Functional Specifications

### 7.1 Customer Portal (`/` & `/customer`)
- **Hero Section:** Banner with GVD Auto World branding, tagline ("Two-Wheeler & Four-Wheeler Precision Care"), quick actions ("Track My Vehicle", "Book Appointment", "Showroom", "Workshop Directions").
- **Live Vehicle Search Bar:** Input vehicle plate number (e.g., `MH12RY1234` or `KA01MJ9821`) to fetch instant live progress status, estimated delivery time, and assigned service advisor.
- **Interactive 360° Health Inspection Carousel:**
  - View angles: Front, Left Side, Rear, Right Side, Engine Bay, Interior, Underbody.
  - Component pills indicating:
    - 🟢 **GOOD** ("Working optimally")
    - 🟡 **SERVICED** ("Replaced / Serviced, good for 5,000 km")
    - 🔴 **NEED REPLACE** ("Critical wear, immediate replacement recommended")
  - Displays replacement cost estimate and technician diagnosis.
- **Service Package Booking Modal:** Select 2W / 4W, choose package (General Service, PPF & Detailing, Ceramic Coating, Brake Overhaul), preferred date & time slot, doorstep pickup toggle.
- **Online Payment Gateway Simulation:** Displays itemized subtotal (Spares + Labour + Lubes + GST), allows 1-click mock payment or Razorpay/Cashfree webhook integration, downloads PDF receipt.
- **Verified Customer Testimonials:** Dynamic list of 5-star customer reviews with verified badges and official workshop replies.
- **Workshop Card:** Shows Vibgyor High School Road, Kundalahalli address with direct 1-tap "Open in Google Maps" button.

---

### 7.2 Service Advisor / Staff Dashboard (`/staff`)
- **Key Metrics KPI Bar:**
  - Active Vehicles in Bay
  - Ready for Delivery
  - Today's Estimated Inward Value (₹)
  - Critical Parts Low Stock Alerts
- **Filterable Job Cards Table:**
  - Columns: Job Card #, Vehicle Plate, Customer Name, Vehicle Type (2W badge / 4W badge), Current Status (Color-coded badge), Total (₹), Balance Due (₹), Delivery Target, Actions.
  - Search by plate number or mobile.
  - Filter chips: All, Open, In Progress, Quality Check, Ready for Delivery.
- **Top Quick Actions:** "Create New Job Card" (Primary button), "Scan Vehicle QR Code", "Inventory Browser", "Technician Bay".

---

### 7.3 Job Card Creation Wizard with 6-Angle Dent Capture (`/staff/job-cards/new`)
A 3-step wizard engineered for fast check-in:

#### Step 1: Customer & Vehicle Information
- Vehicle Number plate (formatted with auto-uppercase).
- Vehicle Type toggle: Two-Wheeler (Bike/Scooter) vs Four-Wheeler (Car/SUV).
- Customer Name, Mobile Number, Email.
- Make (e.g. Bajaj, Tata, Royal Enfield, Hyundai, KTM), Model, Variant.
- Current Odometer Reading (km) & Fuel Level Slider (0% to 100%).
- Accessories Present notes (Luggage rack, Dashcam, Mats).
- Customer Voice / Reported Complaints (Text area).

#### Step 2: 6-Angle Dent & Scratch Documentation (CRITICAL WORKSHOP FEATURE)
Interactive grid containing cards for the 6 mandatory inspection angles:
1. **Front Angle:** Bumper, Hood, Grille, Headlights & Fog Lamps.
2. **Rear Angle:** Boot / Tailgate, Rear Bumper, Taillights & Exhaust.
3. **Left Side Profile:** Left Doors, Front/Rear Fenders, Running Board & Mirrors.
4. **Right Side Profile:** Right Doors, Quarter Panel, Side Skirts & Window Frame.
5. **Roof & Glass:** Roof Panel, Sunroof, Front Windshield & Rear Screen.
6. **Dent / Scratch Close-Up:** Detailed macro zoom of existing scrapes, deep dents, or paint peeling.

**Per Angle Controls:**
- Photo File Upload (supports camera capture on mobile web or drag-and-drop).
- "Has Dent / Scratch" toggle switch.
- Severity selector pills:
  - `NO_DENT` (Green - "No Dents / Clear")
  - `MINOR_SCRATCH` (Amber - "Minor Surface Scratch")
  - `MEDIUM_DENT` (Orange - "Medium Body Dent")
  - `MAJOR_DAMAGE` (Red - "Major Panel Damage")
- Detailed scratch inspection notes input.
- "Load Demo Photos" button (1-click sample dataset for instant testing).

#### Step 3: Parts Picking & Advance Settlement
- Search and pick spares, lubricants, or labour items from master inventory.
- Real-time line item table with quantity and price adjustment.
- Advance amount collected (Cash / UPI / Card).
- Target delivery date and time picker.
- SMS & WhatsApp alert opt-in toggle.
- On Submit: Creates Job Card record, generates unique Job Card Number (`JC-YYYY-XXXX`), and produces a printable QR code pass.

---

### 7.4 Job Card Detail & Billing Management (`/staff/job-cards/[id]`)
- **Status Stepper:** Visual progression pipeline (Open ➔ In Progress ➔ Quality Check ➔ Ready ➔ Completed). Clicking advances the status.
- **Dent Photo Gallery Preview:** Modal lightbox displaying high-resolution photos of all 6 angles captured during check-in, safeguarding against customer disputes on vehicle pickup.
- **Financial Breakdown Card:**
  - Total Spares: ₹ `totalSpares`
  - Total Labour: ₹ `totalLabour`
  - Total Lubes & Fluids: ₹ `totalLubes`
  - Subtotal & GST
  - Advance Paid & Balance Due
- **Quick Action Bar:**
  - 🟢 **Send WhatsApp Invoice:** Generates pre-formatted WhatsApp text with itemized bill and payment link.
  - 🖨️ **Print Job Sheet / Gate Pass:** Generates printable A4 voucher.
  - 💳 **Mark as Paid Online:** Toggles payment status.

---

### 7.5 Technician 360° Inspection Bay (`/technician`)
- List of vehicles currently in status `OPEN` or `IN_PROGRESS`.
- Component Inspection Checklist grouped by vehicle angle:
  - Engine Bay (Engine Oil, Battery, Filters)
  - Braking (Front/Rear Pads, Discs, Brake Fluid)
  - Wheels & Suspension (Tires, Alignment, Shocks)
  - Detailing (PPF, Ceramic, Glass Coat)
  - Climate (AC Filter, Compressor, Throttle Body)
  - Underbody & Exhaust
- Status selector buttons: `GOOD`, `SERVICED`, `NEED_REPLACE`.
- Quick photo upload for defective components (evidence sent to customer).
- "Send for Quality Check" button when all components are inspected.

---

### 7.6 Inventory Management & Excel/CSV Batch Import (`/admin/inventory`)
- Master Inventory Table with Stock Alerts:
  - Red highlight for items where `stockQuantity <= minThresholdAlert`.
  - Filter by category: All, Spare Part, Labour Charge, Lube & Fluids, Detailing.
  - Search by Part Name or Part Number.
  - Stock adjustment stepper (`+` / `-`).

#### Excel & CSV Batch Parts Inward Dialog:
1. **File Upload Dropzone:** Accepts `.xlsx`, `.xls`, or `.csv` files.
2. **Column Auto-Detection:** Automatically maps columns:
   - Part Name / Description (`partName`, `name`, `item`)
   - Part Number / SKU (`partNumber`, `sku`, `code`)
   - Category (`category`, `type` -> `SPARE`, `LUBE`, `LABOUR`, `DETAILING`)
   - Vehicle Type (`vehicleType`, `compatible` -> `TWO_WHEELER`, `FOUR_WHEELER`)
   - Quantity Received (`quantity`, `qty`, `stock`)
   - Unit Purchase/Selling Price (`unitPrice`, `price`, `rate`)
3. **Interactive Preview Table:**
   - Displays all parsed rows before committing.
   - Highlights items that already exist in database (will update quantity and price) vs brand new items (will be inserted).
   - Shows total parts count and total inward invoice amount (₹).
4. **Auto-Generate Purchase Order:** Checkbox to automatically create an inward `PurchaseOrder` in `RECEIVED` status with vendor information and timestamp for audit compliance.
5. **One-Click Sample Loader:** Provides a pre-configured template button allowing anyone to test bulk inwarding instantly without needing a physical spreadsheet file.

---

### 7.7 Purchase Order Generation (`/admin/purchase-orders`)
- Generate official purchase orders for suppliers (e.g. Bangalore OEM Spares Hub on JC Road).
- Filter items below minimum threshold and 1-click "Add Low Stock Items to PO".
- Calculates 18% GST and grand total.
- Formats WhatsApp message to dispatch directly to supplier.
- Export as printable Purchase Order PDF.

---

### 7.8 Vehicle QR Scanner & Pass Generator (`/scanner` & `/pass/[vehicleNumber]`)
- **Web QR Scanner (`/scanner`):**
  - Uses browser camera (`navigator.mediaDevices.getUserMedia`) via `html5-qrcode`.
  - Decodes QR payload format `GVD:VEHICLE:<PLATE>:<JC_NUMBER>`.
  - Automatically redirects to the active Job Card details or pre-fills a new Job Card check-in.
- **Printable Vehicle QR Window Pass (`/pass/[vehicleNumber]`):**
  - Displays vehicle plate number, customer name, date, and large high-contrast QR code.
  - Designed to be taped to the vehicle windshield or bike headlamp during workshop stay.

---

### 7.9 Workshop Location & Navigation (`/location`)
- Displays workshop address: Vibgyor High School Road, GJM Sai Garden Layout, Kundalahalli, Bengaluru 560037.
- Embedded map with custom pin at `12.9551796, 77.717493`.
- 1-Click "Navigate with Google Maps" targeting `https://maps.app.goo.gl/xgdEGHBKRth1TUrs7`.
- "Share Location via WhatsApp" button with pre-filled message for customers.

---

## 8. Core Business Logic & Algorithms

### 8.1 Invoice & Financial Calculations

```typescript
export interface InvoiceTotals {
  totalSpares: number;
  totalLabour: number;
  totalLubes: number;
  totalDetailing: number;
  subtotal: number;
  gstAmount: number; // 18% on Labour & Spares
  grandTotal: number;
  advancePaid: number;
  balanceDue: number;
}

export function calculateJobCardTotals(items: Array<{
  category: 'SPARE' | 'LABOUR' | 'LUBE' | 'DETAILING';
  unitPrice: number;
  quantity: number;
  discount: number;
}>, advancePaid: number = 0): InvoiceTotals {
  let totalSpares = 0;
  let totalLabour = 0;
  let totalLubes = 0;
  let totalDetailing = 0;

  for (const item of items) {
    const lineTotal = Math.max(0, (item.unitPrice * item.quantity) - item.discount);
    switch (item.category) {
      case 'SPARE':
        totalSpares += lineTotal;
        break;
      case 'LABOUR':
        totalLabour += lineTotal;
        break;
      case 'LUBE':
        totalLubes += lineTotal;
        break;
      case 'DETAILING':
        totalDetailing += lineTotal;
        break;
    }
  }

  const subtotal = totalSpares + totalLabour + totalLubes + totalDetailing;
  const gstAmount = Math.round((subtotal * 0.18) * 100) / 100; // 18% standard GST
  const grandTotal = Math.round((subtotal + gstAmount) * 100) / 100;
  const balanceDue = Math.max(0, grandTotal - advancePaid);

  return {
    totalSpares,
    totalLabour,
    totalLubes,
    totalDetailing,
    subtotal,
    gstAmount,
    grandTotal,
    advancePaid,
    balanceDue,
  };
}
```

---

### 8.2 Excel & CSV Parts Import Parser (SheetJS)

```typescript
import * as XLSX from 'xlsx';

export interface ParsedItem {
  partName: string;
  partNumber: string;
  category: 'SPARE' | 'LABOUR' | 'LUBE' | 'DETAILING';
  compatibleType: 'TWO_WHEELER' | 'FOUR_WHEELER';
  unitPrice: number;
  quantity: number;
  unit: string;
}

export function parseInventoryFile(fileBuffer: ArrayBuffer): ParsedItem[] {
  const workbook = XLSX.read(fileBuffer, { type: 'array' });
  const firstSheetName = workbook.SheetNames[0];
  const worksheet = workbook.Sheets[firstSheetName];
  const rawRows: Record<string, any>[] = XLSX.utils.sheet_to_json(worksheet, { defval: '' });

  return rawRows.map((row, idx) => {
    // Flexible header mapping
    const partName = row['Part Name'] || row['Item Name'] || row['Description'] || `Part ${idx + 1}`;
    const partNumber = row['Part Number'] || row['SKU'] || row['Code'] || `GEN-${Date.now()}-${idx}`;
    
    // Normalize Category
    const rawCat = String(row['Category'] || '').toUpperCase();
    let category: 'SPARE' | 'LABOUR' | 'LUBE' | 'DETAILING' = 'SPARE';
    if (rawCat.includes('LUBE') || rawCat.includes('OIL')) category = 'LUBE';
    else if (rawCat.includes('LABOUR') || rawCat.includes('SERVICE')) category = 'LABOUR';
    else if (rawCat.includes('DETAIL') || rawCat.includes('PPF') || rawCat.includes('CERAMIC')) category = 'DETAILING';

    // Normalize Vehicle Type
    const rawVeh = String(row['Vehicle Type'] || row['Type'] || '').toUpperCase();
    const compatibleType = (rawVeh.includes('4W') || rawVeh.includes('CAR')) ? 'FOUR_WHEELER' : 'TWO_WHEELER';

    const quantity = parseInt(row['Quantity'] || row['Qty'] || '1', 10) || 1;
    const unitPrice = parseFloat(row['Unit Price'] || row['Price'] || row['Rate'] || '0') || 0;
    const unit = String(row['Unit'] || 'pcs').toLowerCase();

    return {
      partName: String(partName).trim(),
      partNumber: String(partNumber).trim(),
      category,
      compatibleType,
      unitPrice,
      quantity,
      unit,
    };
  });
}
```

---

### 8.3 QR Code Payload Structure

The QR code encodes a standardized text string for high-speed scanning:
```
GVD:VEHICLE:<VEHICLE_NUMBER>:<JOB_CARD_NUMBER>
```
*Example:* `GVD:VEHICLE:MH12RY1234:JC-2026-0427`

**Decoder Logic:**
```typescript
export function decodeQrPayload(payload: string) {
  if (!payload.startsWith('GVD:VEHICLE:')) return null;
  const parts = payload.split(':');
  if (parts.length >= 4) {
    return {
      vehicleNumber: parts[2],
      jobCardNumber: parts[3],
    };
  }
  return null;
}
```

---

### 8.4 WhatsApp & SMS Notification Templates

#### 1. Invoice & Delivery WhatsApp Message:
```text
🏁 *GVD AUTO WORLD — DIGITAL INVOICE & VEHICLE PASS*
📍 Vibgyor High School Road, Kundalahalli, Bengaluru 560037
Maps: https://maps.app.goo.gl/xgdEGHBKRth1TUrs7

Dear {customerName},
Your vehicle *{vehicleNumber}* ({make} {model}) is *{statusLabel}*!

📋 *Job Card:* {jobCardNumber}
🛠️ *Total Spares:* ₹{totalSpares}
🔧 *Total Labour:* ₹{totalLabour}
🛢️ *Lubes & Fluids:* ₹{totalLubes}
---------------------------------
💰 *Total Amount:* ₹{totalAmount}
💵 *Advance Paid:* ₹{advancePaid}
💳 *Balance Due:* ₹{balanceAmount}

🔍 *View 360° Digital Inspection & Pay Online:*
https://gvdautoworld.com/track/{vehicleNumber}

Call Support: +91 98450 12345
Thank you for choosing GVD Auto World!
```

#### 2. Supplier Purchase Order WhatsApp Message:
```text
📦 *PURCHASE ORDER — GVD AUTO WORLD BANGALORE*
PO Number: {poNumber}
Date: {currentDate}
Supplier: {supplierName}
Delivery Hub: Vibgyor High School Road, Kundalahalli, Bengaluru 560037
Maps: https://maps.app.goo.gl/xgdEGHBKRth1TUrs7

Items Required:
{lineItemsList}

Total Units: {totalUnits}
Estimated Total: ₹{grandTotal} (inc. 18% GST)

Please confirm dispatch date and invoice copy.
```

---

## 9. REST API Endpoint Specifications

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/v1/job-cards` | List all job cards with status/search filters | Staff / Admin |
| `POST` | `/api/v1/job-cards` | Create new job card with 6 dent photos | Staff / Admin |
| `GET` | `/api/v1/job-cards/:id` | Get job card details and line items | All |
| `PATCH` | `/api/v1/job-cards/:id/status` | Advance status (`OPEN` ➔ `IN_PROGRESS` ➔ `READY`) | Staff / Tech / Admin |
| `POST` | `/api/v1/job-cards/:id/items` | Add spare part or labour item | Staff / Admin |
| `GET` | `/api/v1/vehicles/:vehNo/inspections` | Get 360° component health report | Customer / Tech |
| `PUT` | `/api/v1/vehicles/:vehNo/inspections` | Update component health status & diagnosis | Technician / Admin |
| `GET` | `/api/v1/inventory` | List parts with low-stock alerts | Staff / Admin |
| `POST` | `/api/v1/inventory/batch-import` | Upload Excel/CSV parts file (Multipart) | Admin |
| `POST` | `/api/v1/purchase-orders` | Create inward supplier purchase order | Admin |
| `GET` | `/api/v1/showroom` | List pre-owned vehicles available for sale | Public / Customer |
| `POST` | `/api/v1/reviews` | Submit new customer review & rating | Customer |
| `PATCH` | `/api/v1/reviews/:id/reply` | Submit official admin reply to review | Admin |
| `POST` | `/api/v1/appointments` | Book service appointment & pickup | Public / Customer |

---

## 10. Step-by-Step Machine Setup & Migration Roadmap

### Phase 1: Environment & Project Initialization (30 Minutes)
1. On your new machine, initialize a fresh Next.js project with TypeScript and Tailwind CSS:
   ```bash
   npx create-next-app@latest gvd-auto-world-web --typescript --tailwind --eslint --app
   cd gvd-auto-world-web
   ```
2. Install the necessary ecosystem dependencies:
   ```bash
   npm install @prisma/client lucide-react clsx tailwind-merge xlsx papaparse html5-qrcode qrcode.react @radix-ui/react-dialog @radix-ui/react-tabs @radix-ui/react-select canvas-confetti
   npm install -D prisma @types/papaparse @types/canvas-confetti
   ```
3. Initialize Prisma ORM:
   ```bash
   npx prisma init
   ```

### Phase 2: Database Setup & Seed Import (15 Minutes)
1. Point your `.env` `DATABASE_URL` to a PostgreSQL instance (e.g., local Postgres, Supabase, Neon, or Railway):
   ```env
   DATABASE_URL="postgresql://postgres:password@localhost:5432/gvd_auto_world?schema=public"
   ```
2. Copy the `schema.prisma` definition provided in [Section 3.2](#32-prisma-schema-schemaprisma) into your `prisma/schema.prisma` file.
3. Run the database migration:
   ```bash
   npx prisma migrate dev --name init_gvd_schema
   ```
4. Execute the SQL Seed Script from [Section 4.1](#41-sql-insert-scripts) in pgAdmin, DBeaver, or via Prisma Seed script:
   ```bash
   npx prisma db execute --file ./seed.sql
   ```

### Phase 3: Route Structure Implementation
Create the following page hierarchy in `src/app/`:
```text
src/app/
├── layout.tsx
├── page.tsx                             // Customer Portal & Live Vehicle Search
├── track/[vehicleNumber]/page.tsx       // 360° Health Report & Online Payment
├── location/page.tsx                    // Google Map Kundalahalli Workshop Pin
├── staff/
│   ├── page.tsx                         // Service Advisor Dashboard & Active Cards
│   └── job-cards/
│       ├── new/page.tsx                 // 3-Step Wizard with 6 Dent Photos
│       └── [id]/page.tsx                // Job Card Detail, Billing, WhatsApp Share
├── technician/page.tsx                  // 360° Inspection Bay & Status Checklist
├── admin/
│   ├── inventory/page.tsx               // Master Inventory & Excel Import Dialog
│   ├── purchase-orders/page.tsx         // PO Generator & Supplier WhatsApp
│   └── reviews/page.tsx                 // Review Moderation & Admin Responses
├── scanner/page.tsx                     // Web Camera QR Code Scanner
└── api/                                 // Next.js API Routes (from Section 9)
```

### Phase 4: Production Deployment
- **Frontend / Fullstack:** Deploy to Vercel or Docker container.
- **Database:** Supabase or Neon Serverless Postgres.
- **Storage:** Cloudflare R2 bucket for storing the 6-angle dent photos and inspection images.
- **Maps:** Google Maps Embed API or Leaflet with OpenStreetMap tiles.

---
*Generated for GVD Auto World engineering handover. All technical specifications, schemas, business formulas, and raw data are accurate and ready for immediate deployment.*
