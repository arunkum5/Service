package com.example.model

enum class UserRole {
    CUSTOMER,
    TECHNICIAN,
    STAFF,
    ADMIN
}

enum class VehicleType(val label: String) {
    TWO_WHEELER("2W (Bike/Scooter)"),
    FOUR_WHEELER("4W (Car/SUV)")
}

enum class ComponentStatus(val label: String) {
    GOOD("Good"),
    SERVICED("Serviced (Works till next service)"),
    NEED_REPLACE("Need to Replace")
}

enum class JobCardStatus(val label: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    QUALITY_CHECK("Quality Check"),
    READY("Ready for Delivery"),
    COMPLETED("Completed"),
    CLOSED("Closed")
}

enum class ItemCategory(val label: String) {
    SPARE("Spare Part"),
    LABOUR("Labour Charge"),
    LUBE("Lube & Fluids"),
    DETAILING("Detailing & PPF")
}

enum class VehicleViewAngle(val label: String, val description: String) {
    FRONT("Front View", "Bumper, Headlights, Radiator & Hood"),
    SIDE_LEFT("Left Profile", "Wheels, Tires, Brakes & Side Panels"),
    REAR("Rear View", "Tail Lights, Exhaust, Bumper & Boot"),
    SIDE_RIGHT("Right Profile", "Right Tires, PPF Coating & Doors"),
    ENGINE_BAY("Engine Bay", "Engine Oil, Coolant, Battery & Filters"),
    INTERIOR("Interior Cabin", "Dashboard, AC Filter & Deep Clean Seats"),
    UNDERBODY("Underbody", "Suspension, Chassis & Exhaust Line")
}
