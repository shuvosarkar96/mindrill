package com.mindrill.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(val label: String, val icon: ImageVector) {
    Electronics("Electronics", Icons.Filled.Computer),
    Electric("Electric", Icons.Filled.ElectricalServices),
    Drilling("Drilling & heavy", Icons.Filled.Construction),
    Safety("Safety gear", Icons.Filled.Engineering),
    Tools("Tools & parts", Icons.Filled.Handyman),
    Wanted("Wanted", Icons.Filled.ShoppingBag)
}

data class Product(
    val id: Int,
    val name: String,
    val priceUsd: Int,
    val category: Category,
    val seller: String,
    val condition: String,
    val details: String,
    val imagePath: String? = null
)

fun Product.priceLabel(): String =
    if (category == Category.Wanted) "Budget %,d USD".format(priceUsd) else "%,d USD".format(priceUsd)

private fun p(
    id: Int, name: String, price: Int, cat: Category,
    seller: String, condition: String, details: String
) = Product(id, name, price, cat, seller, condition, details)

val sampleProducts: List<Product> = listOf(
    // Electronics
    p(1, "Metal Detector CTX3030", 5000, Category.Electronics, "Ali Ibrahim", "Used, working", "Gold detection machine. Manufacturer: Loop Techno Systems, India. Year: 2018."),
    p(2, "Ground-Penetrating Radar", 12000, Category.Electronics, "Dhaka Mining Supply", "New", "Subsurface scanning unit for mineral and void detection."),
    p(3, "Handheld GPS Survey Unit", 650, Category.Electronics, "Rahim Traders", "New", "Rugged GPS for marking claims and survey points."),
    p(4, "Rugged Mining Tablet", 780, Category.Electronics, "Dhaka Mining Supply", "New", "Dust and water resistant tablet with long battery life."),
    p(5, "Drone Mapping Kit", 2400, Category.Electronics, "Sylhet Gear Market", "New", "Drone with camera for site mapping and stockpile volume."),
    p(6, "Walkie-Talkie Set (4 pcs)", 180, Category.Electronics, "Nasrin Supplies", "New", "Long-range radios for crews working underground or in quarries."),
    p(7, "Total Station Surveying Kit", 4200, Category.Electronics, "Karim Drilling Co.", "Used, working", "Precision surveying instrument with tripod."),
    // Electric
    p(8, "Portable Generator 5 kW", 1200, Category.Electric, "Rahim Traders", "New", "Petrol generator for remote sites."),
    p(9, "Submersible Water Pump", 420, Category.Electric, "Khulna Tools Bazar", "New", "Heavy-duty pump for dewatering pits."),
    p(10, "Cable Reel 50 m", 160, Category.Electric, "Nasrin Supplies", "New", "Industrial extension reel with overload protection."),
    p(11, "LED Floodlight Tower", 950, Category.Electric, "Sylhet Gear Market", "New", "Mobile lighting tower for night shifts."),
    p(12, "Battery Inverter 3 kVA", 540, Category.Electric, "Dhaka Mining Supply", "New", "Backup power for camp equipment."),
    p(13, "Welding Machine 250 A", 480, Category.Electric, "Khulna Tools Bazar", "New", "Machine for field repairs of frames and buckets."),
    // Drilling & heavy
    p(14, "Diamond Core Drill Rig", 18500, Category.Drilling, "Karim Drilling Co.", "Used, working", "Core sampling rig for exploration drilling."),
    p(15, "Electric Jack Hammer 1700 W", 3000, Category.Drilling, "Ali Ibrahim", "New", "1700 watt, light weight demolition hammer."),
    p(16, "Gold Washing Plant", 50000, Category.Drilling, "Ali Ibrahim", "Used, working", "Gold wash process and equipment set."),
    p(17, "Jaw Rock Crusher", 22000, Category.Drilling, "Karim Drilling Co.", "New", "Primary crusher for hard rock."),
    p(18, "Hydraulic Rock Breaker", 9800, Category.Drilling, "Dhaka Mining Supply", "Used, working", "Excavator-mounted breaker."),
    p(19, "Vibrating Sluice Box", 450, Category.Drilling, "Rahim Traders", "New", "Portable sluice for small-scale washing."),
    p(20, "Portable Air Compressor", 2600, Category.Drilling, "Khulna Tools Bazar", "New", "Compressor to drive pneumatic drills."),
    // Safety gear
    p(21, "Helmet with Headlamp", 85, Category.Safety, "Nasrin Supplies", "New", "Hard hat with rechargeable lamp."),
    p(22, "Respirator Mask Set", 60, Category.Safety, "Nasrin Supplies", "New", "Dust and fume protection with spare filters."),
    p(23, "Steel-Toe Boots", 95, Category.Safety, "Rahim Traders", "New", "Slip resistant safety boots."),
    p(24, "Hi-vis Vest 5-pack", 40, Category.Safety, "Rahim Traders", "New", "Reflective vests in safety orange."),
    p(25, "4-Gas Detector", 620, Category.Safety, "Dhaka Mining Supply", "New", "Detects oxygen, CO, H2S and combustible gas."),
    p(26, "Ear Protection Muffs", 20, Category.Safety, "Nasrin Supplies", "New", "Noise reduction for drilling and crushing areas."),
    // Tools & parts
    p(27, "Rock Drill Bit Set", 210, Category.Tools, "Khulna Tools Bazar", "New", "Carbide tipped bits in assorted sizes."),
    p(28, "Pickaxe and Shovel Kit", 55, Category.Tools, "Khulna Tools Bazar", "New", "Forged steel hand tools."),
    p(29, "Conveyor Belt 10 m", 780, Category.Tools, "Sylhet Gear Market", "New", "Rubber belt for material handling."),
    p(30, "Sample Bags (500)", 30, Category.Tools, "Nasrin Supplies", "New", "Numbered bags for ore samples."),
    p(31, "Hydraulic Hose Kit", 140, Category.Tools, "Dhaka Mining Supply", "New", "Hoses and fittings for heavy machines."),
    p(32, "Geologist Rock Hammer", 25, Category.Tools, "Khulna Tools Bazar", "New", "Chisel-tip hammer for field sampling."),
    // Wanted
    p(33, "Used excavator", 25000, Category.Wanted, "Karim Drilling Co.", "Wanted", "Looking for a 20 t excavator in working condition."),
    p(34, "Dump truck 10 t", 30000, Category.Wanted, "Sylhet Gear Market", "Wanted", "Need a tipper truck for quarry transport."),
    p(35, "Core sample tester", 4000, Category.Wanted, "Ali Ibrahim", "Wanted", "Looking for lab equipment to test core samples."),
    p(36, "Mineral assay service", 1500, Category.Wanted, "Rahim Traders", "Wanted", "Need a lab to assay ore samples.")
)