package org.smilelee.eventhorizonfileanalyzer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.smilelee.eventhorizonfileanalyzer.StringFormat.appendTo
import org.smilelee.eventhorizonfileanalyzer.StringFormat.formatTo
import java.io.File

val dirNames = hashMapOf(
        1 to "Component\\",
        2 to "Device\\",
        3 to "Weapon\\",
        4 to "Ammunition\\",
        5 to "DroneBay\\",
        6 to "Ship\\",
        7 to "Satellite\\",
        8 to "Ship\\Build\\",
        9 to "Satellite\\Build\\",
        10 to "Technology\\",
        11 to "Component\\Stats\\",
        12 to "Component\\Mod\\",
        13 to "TechnologyMap\\",
        100 to ""
)

class Data {
    val components = HashMap<Int, Component>()
    val devices = HashMap<Int, Device>()
    val weapons = HashMap<Int, Weapon>()
    val ammunition = HashMap<Int, Ammunition>()
    val droneBays = HashMap<Int, DroneBay>()
    val ships = HashMap<Int, Ship>()
    val satellites = HashMap<Int, Satellite>()
    val shipBuilds = HashMap<Int, ShipBuild>()
    val satelliteBuilds = HashMap<Int, SatelliteBuild>()
    val technology = HashMap<Int, Technology>()
    val componentStats = HashMap<Int, ComponentStats>()
    val componentModifications = HashMap<Int, ComponentModification>()
    val technologyMap = HashMap<Int, TechnologyMap>()
    lateinit var shipBuilderSettings: ShipBuilderSettings
    
    enum class Faction(val factionName: String) {
        FREE_STARS("Free Stars"),
        VENIRI("Veniri    "),
        BUSHARK("Bushark   "),
        KORERANS("Korerans  "),
        TARANIAKS("Taraniaks "),
        SAYJIX("Sayjix    "),
        JURGANS("Jurgans   "),
        ZUMBALARI("Zumbalari "),
        NEGANARI("Neganari  "),
        DAAZEN("Daazen    "),
        UNKNOWNS("Unknowns  "),
        THE_EMPIRE("The Empire"),
        THE_SWARM("The Swarm "),
        ;
        
        companion object {
            fun factionFromId(id: Int) = when (id) {
                -1, 0 -> FREE_STARS
                1     -> VENIRI
                2     -> BUSHARK
                3     -> KORERANS
                4     -> TARANIAKS
                5     -> SAYJIX
                6     -> JURGANS
                7     -> ZUMBALARI
                8     -> NEGANARI
                9     -> DAAZEN
                10    -> UNKNOWNS
                11    -> THE_EMPIRE
                12    -> THE_SWARM
                else  -> throw IllegalArgumentException()
            }
        }
    }
    
    abstract class EHDataFile {
        val fileName: String get() = file.name
        lateinit var file: File
        lateinit var rawContent: ByteArray
        lateinit var content: String
        
        abstract val itemType: Int
        abstract val id: Int
        
        fun databaseFileName(path: String) =
                "$path${dirNames[itemType]}${id}_${databaseRawFileName()}.json"
        
        abstract fun databaseRawFileName(): String
        
        lateinit var data: Data
        
        abstract val name: String
        
        open fun addToData(data: Data) {
            this.data = data
        }
    }
    
    val fileMapFromItemType: Map<Int, Map<Int, EHDataFile>> by lazy {
        mapOf(
                1 to components,
                2 to devices,
                3 to weapons,
                4 to ammunition,
                5 to droneBays,
                6 to ships,
                7 to satellites,
                8 to shipBuilds,
                9 to satelliteBuilds,
                10 to technology,
                11 to componentStats,
                12 to componentModifications,
                13 to technologyMap,
                100 to mapOf(0 to shipBuilderSettings)
        )
    }
    
    object ItemType {
        const val COMPONENT = 1
        const val DEVICE = 2
        const val WEAPON = 3
        const val AMMUNITION = 4
        const val DRONE_BAY = 5
        const val SHIP = 6
        const val SATELLITE = 7
        const val SHIP_BUILD = 8
        const val SATELLITE_BUILD = 9
        const val TECHNOLOGY = 10
        const val COMPONENT_STATS = 11
        const val COMPONENT_MODIFICATION = 12
        const val TECHNOLOGY_MAP = 13
    }
    
    data class Component(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.COMPONENT,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("Name") override val name: String = "",
            @Expose @SerializedName("DisplayCategory") val displayCategory: Int = 0,
            @Expose @SerializedName("Availability") val availability: Int = 1,
            @Expose @SerializedName("ComponentStatsId") val componentStatsId: Int = 0,
            @Expose @SerializedName("Faction") val factionId: Int = -1,
            @Expose @SerializedName("Level") val level: Int = 0,
            @Expose @SerializedName("Layout") val layout: String = "1",
            @Expose @SerializedName("CellType") val cellType: String = "",
            @Expose @SerializedName("DeviceId") val deviceId: Int = -1,
            @Expose @SerializedName("DroneBayId") val droneBayId: Int = -1,
            @Expose @SerializedName("DroneId") val droneId: Int = -1,
            @Expose @SerializedName("WeaponId") val weaponId: Int = -1,
            @Expose @SerializedName("AmmunitionId") val ammunitionId: Int = -1,
            @Expose @SerializedName("WeaponSlotType") val weaponSlotType: String = "",
            @Expose @SerializedName("PossibleModifications") val possibleModifications: List<Int> = listOf()
    ) : EHDataFile() {
        override fun toString() = "$fileName Component[${formatTo(id, 3)}] " +
                "from ${Faction.factionFromId(factionId).factionName}: $name"
        
        val isDevice get() = deviceId != -1
        val isDroneBay get() = droneBayId != -1
        val isWeapon get() = weaponId != -1
        
        override fun databaseRawFileName() = name
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.components[id] = this
        }
    }
    
    data class Position(
            val x: Int = 0,
            val y: Int = 0
    ) {
        override fun toString() = "($x, $y)"
    }
    
    data class Device(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.DEVICE,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("DeviceClass") val deviceClass: Int = 0,
            @Expose @SerializedName("EnergyConsumption") val energyConsumption: Double = 0.0,
            @Expose @SerializedName("Power") val power: Double = 0.0,
            @Expose @SerializedName("Range") val range: Double = 0.0,
            @Expose @SerializedName("Size") val size: Double = 0.0,
            @Expose @SerializedName("Cooldown") val coolDown: Double = 0.0,
            @Expose @SerializedName("Offset") val offset: Position = Position()
    ) : EHDataFile() {
        override fun toString() = "$fileName Device[${formatTo(id, 3)}]"
        
        override fun databaseRawFileName() = "Device"
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.devices[id] = this
        }
        
        override val name: String get() = "Device$id"
    }
    
    data class Weapon(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.WEAPON,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("WeaponClass") val weaponClass: Int = 0,
            @Expose @SerializedName("FireRate") val fireRate: Double = 1.0,
            @Expose @SerializedName("Spread") val spread: Double = 0.0,
            @Expose @SerializedName("Magazine") val magazine: Int = 1
    ) : EHDataFile() {
        override fun toString() = "$fileName Weapon[${formatTo(id, 3)}] of $name"
        
        override val name: String
            get() = data.components.filter { (_, component) -> component.ammunitionId == id }
                    .let { weapons ->
                        when {
                            weapons.isEmpty() -> data.ammunition.filter { (_, ammunition) ->
                                ammunition.coupledAmmunitionId == id
                            }.let { ammunition ->
                                        when {
                                            ammunition.isEmpty() -> "none"
                                            ammunition.size > 1  -> "couple of many"
                                            else                 -> "couple of " + ammunition.values.first().name
                                        }
                                    }
                            weapons.size > 1  -> "many"
                            else              -> weapons.values.first().name
                        }
                    }
        
        override fun databaseRawFileName() = name
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.weapons[id] = this
        }
    }
    
    data class Ammunition(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.AMMUNITION,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("AmmunitionClass") val ammunitionClass: Int = 0,
            @Expose @SerializedName("DamageType") val damageType: Int = 0,
            @Expose @SerializedName("Impulse") val impulse: Double = 0.0,
            @Expose @SerializedName("Recoil") val recoil: Double = 0.0,
            @Expose @SerializedName("Size") val size: Double = 0.0,
            @Expose @SerializedName("AreaOfEffect") val areaOfEffect: Double = 0.0,
            @Expose @SerializedName("Damage") val damage: Double = 0.0,
            @Expose @SerializedName("Range") val range: Double = 0.0,
            @Expose @SerializedName("Velocity") val velocity: Double = 0.0,
            @Expose @SerializedName("LifeTime") val lifeTime: Double = 0.0,
            @Expose @SerializedName("HitPoints") val hitPoints: Double = Double.MAX_VALUE,
            @Expose @SerializedName("EnergyCost") val energyCost: Double = 0.0,
            @Expose @SerializedName("CoupledAmmunitionId") val coupledAmmunitionId: Int = -1
    ) : EHDataFile() {
        override fun toString() = "$fileName Ammunition[${formatTo(id, 3)}] of $name"
        
        override val name: String
            get() = data.components.filter { (_, component) -> component.ammunitionId == id }
                    .let { weapons ->
                        when {
                            weapons.isEmpty() -> data.ammunition.filter { (_, ammunition) ->
                                ammunition.coupledAmmunitionId == id
                            }.let { ammunition ->
                                        when {
                                            ammunition.isEmpty() -> "none"
                                            ammunition.size > 1  -> "couple of many"
                                            else                 -> "couple of " + ammunition.values.first().name
                                        }
                                    }
                            weapons.size > 1  -> "many"
                            else              -> weapons.values.first().name
                        }
                    }
        
        override fun databaseRawFileName() = name
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.ammunition[id] = this
        }
    }
    
    data class DroneBay(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.DRONE_BAY,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("EnergyConsumption") val energyConsumption: Double = 0.0,
            @Expose @SerializedName("PassiveEnergyConsumption") val passiveEnergyConsumption: Double = 0.0,
            @Expose @SerializedName("Range") val range: Double = 0.0,
            @Expose @SerializedName("Capacity") val capacity: Int = 1
    ) : EHDataFile() {
        override fun toString() = "$fileName DroneBay[${formatTo(id, 3)}]"
        
        override fun databaseRawFileName() = "DroneBay"
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.droneBays[id] = this
        }
        
        override val name get() = "DroneBay$id"
    }
    
    data class Barrel(
            @Expose @SerializedName("Position") val position: Position = Position(),
            @Expose @SerializedName("Rotation") val rotation: Double = 0.0,
            @Expose @SerializedName("WeaponClass") val weaponClass: String = "",
            @Expose @SerializedName("PlatformType") val platformType: Int = 0,
            @Expose @SerializedName("Faction") val factionId: Int = -1
    )
    
    data class Ship(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.SHIP,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("Name") override val name: String = "",
            @Expose @SerializedName("Faction") val factionId: Int = -1,
            @Expose @SerializedName("SizeClass") val sizeClass: Int = 0,
            @Expose @SerializedName("Layout") val layout: String = "0",
            @Expose @SerializedName("Barrels") val barrels: List<Barrel> = listOf()
    ) : EHDataFile() {
        override fun toString() = "$fileName Ship[${formatTo(id, 3)}] " +
                "from ${Faction.factionFromId(factionId).factionName}: $name"
        
        override fun databaseRawFileName() = name
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.ships[id] = this
        }
    }
    
    data class Satellite(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.SATELLITE,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("Name") override val name: String = "",
            @Expose @SerializedName("Layout") val layout: String = "0",
            @Expose @SerializedName("Barrels") val barrels: List<Barrel> = listOf()
    ) : EHDataFile() {
        override fun toString() = "$fileName Satellite[${formatTo(id, 3)}]: $name"
        
        override fun databaseRawFileName() = name
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.satellites[id] = this
        }
    }
    
    data class BuildComponent(
            @Expose @SerializedName("ComponentId") val componentId: Int = 0,
            @Expose @SerializedName("Locked") val locked: Boolean = false,
            @Expose @SerializedName("X") val x: Int = 0,
            @Expose @SerializedName("Y") val y: Int = 0,
            @Expose @SerializedName("BarrelId") val barrelId: Int = -1,
            @Expose @SerializedName("Behaviour") val behaviour: Int = 0,
            @Expose @SerializedName("KeyBinding") val keyBinding: Int = -1
    ) {
        override fun toString() = StringFormat.appendTo(data.components[componentId]?.name, 30)
        
        lateinit var data: Data
    }
    
    data class ShipBuild(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.SHIP_BUILD,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("ShipId") val shipId: Int = -1,
            @Expose @SerializedName("DifficultyClass") val difficultyClass: Int = 0,
            @Expose @SerializedName("NotAvailableInGame") val notAvailableInGame: Boolean = false,
            @Expose @SerializedName("Components") val components: List<BuildComponent> = listOf()
    ) : EHDataFile() {
        private val shipName get() = data.ships[shipId]?.name
        
        override val name get() = "$difficultyClass $shipName"
        
        override fun toString() = "$fileName ShipBuild[${formatTo(id, 3)}] level $difficultyClass of $shipName"
        
        override fun databaseRawFileName() = "${shipName}_$difficultyClass"
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.shipBuilds[id] = this
        }
    }
    
    data class SatelliteBuild(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.SATELLITE_BUILD,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("ShipId") val shipId: Int = -1,
            @Expose @SerializedName("DifficultyClass") val difficultyClass: Int = 0,
            @Expose @SerializedName("NotAvailableInGame") val notAvailableInGame: Boolean = false,
            @Expose @SerializedName("Components") val components: List<BuildComponent> = listOf()
    ) : EHDataFile() {
        private val satelliteName get() = data.satellites[id]?.name
        
        override val name get() = "$difficultyClass $satelliteName"
        
        override fun toString() = "$fileName SatelliteBuild[${formatTo(id, 3)}] level $difficultyClass " +
                "of $satelliteName"
        
        override fun databaseRawFileName() = "${satelliteName}_$difficultyClass"
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.satelliteBuilds[id] = this
        }
    }
    
    data class Technology(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.TECHNOLOGY,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("Type") val type: Int = 0,
            @Expose @SerializedName("ItemId") val itemId: Int = 0,
            @Expose @SerializedName("Faction") val factionId: Int = -1,
            @Expose @SerializedName("Price") val price: Int = 0,
            @Expose @SerializedName("Hidden") val hidden: Boolean = false,
            @Expose @SerializedName("Dependencies") val dependencies: List<Int> = listOf()
    ) : EHDataFile() {
        override fun toString() = "$fileName Technology[${formatTo(id, 3)}] " +
                "from ${Faction.factionFromId(factionId).factionName}" +
                ": ${appendTo(name, 30)} depending on [${dependencies.joinToString()}]"
        
        override val name: String
            get() = when (type) {
                0    -> data.components[itemId]?.name
                1    -> data.ships[itemId]?.name
                2    -> data.satellites[itemId]?.name
                else -> null
            } ?: ""
        
        override fun databaseRawFileName() = name
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.technology[id] = this
        }
    }
    
    data class ComponentStats(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.COMPONENT_STATS,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("Type") val type: Int = 0,
            @Expose @SerializedName("ArmorPoints") val armorPoints: Double = 0.0,
            @Expose @SerializedName("EnergyPoints") val energyPoints: Double = 0.0,
            @Expose @SerializedName("EnergyRechargeRate") val energyRechargeRate: Double = 0.0,
            @Expose @SerializedName("Weight") val weight: Double = 0.0,
            @Expose @SerializedName("RammingDamage") val rammingDamage: Double = 0.0,
            @Expose @SerializedName("EnergyAbsorption") val energyAbsorption: Double = 0.0,
            @Expose @SerializedName("KineticResistance") val kineticResistance: Double = 0.0,
            @Expose @SerializedName("EnergyResistance") val energyResistance: Double = 0.0,
            @Expose @SerializedName("ThermalResistance") val thermalResistance: Double = 0.0,
            @Expose @SerializedName("EnginePower") val enginePower: Double = 0.0,
            @Expose @SerializedName("TurnRate") val turnRate: Double = 0.0,
            @Expose @SerializedName("WeaponFireRateModifier") val weaponFireRateModifier: Double = 0.0,
            @Expose @SerializedName("WeaponDamageModifier") val weaponDamageModifier: Double = 0.0,
            @Expose @SerializedName("WeaponRangeModifier") val weaponRangeModifier: Double = 0.0,
            @Expose @SerializedName("WeaponEnergyCostModifier") val weaponEnergyCostModifier: Double = 0.0,
            @Expose @SerializedName("DroneRangeModifier") val droneRangeModifier: Double = 0.0,
            @Expose @SerializedName("DroneDamageModifier") val droneDamageModifier: Double = 0.0,
            @Expose @SerializedName("DroneDefenseModifier") val droneDefenseModifier: Double = 0.0,
            @Expose @SerializedName("DroneSpeedModifier") val droneSpeedModifier: Double = 0.0,
            @Expose @SerializedName("DronesBuiltPerSecond") val dronesBuiltPerSecond: Double = 0.0
    ) : EHDataFile() {
        override fun toString() = "$fileName ComponentStats[${formatTo(id, 3)}] of $name"
        
        override val name: String
            get() = data.components.filter { (_, component) -> component.componentStatsId == id }
                    .let { components ->
                        when {
                            components.isEmpty() -> "none"
                            components.size > 1  -> when {
                                components.values.first().isDevice   -> "DefaultDevice"
                                components.values.first().isDroneBay -> "DefaultDroneBay"
                                components.values.first().isWeapon   -> "DefaultWeapon"
                                else                                 -> "many"
                            }
                            else                 -> components.values.first().name
                        }
                    }
        
        override fun databaseRawFileName() = name
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.componentStats[id] = this
        }
    }
    
    data class ComponentModification(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.COMPONENT_MODIFICATION,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("Type") val deviceClass: Int = 0
    ) : EHDataFile() {
        override fun toString() = "$fileName ComponentModification[${formatTo(id, 3)}]"
        
        override fun databaseRawFileName() = "Modification"
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.componentModifications[id] = this
        }
        
        override val name get() = "ComponentModification$id"
    }
    
    data class TechnologyMap(
            @Expose @SerializedName("ItemType") override val itemType: Int = ItemType.TECHNOLOGY_MAP,
            @Expose @SerializedName("Id") override val id: Int = 0,
            @Expose @SerializedName("Type") val type: Int = 0,
            @Expose @SerializedName("Detail") val detail: Int = 0,
            @Expose @SerializedName("Price") val price: Int = 0,
            @Expose @SerializedName("Dependencies") val dependencies: List<Int> = listOf()
    ) : EHDataFile() {
        override fun toString() = "$fileName TechnologyMap[${formatTo(id, 3)}]: ${appendTo(name, 30)} " +
                "depending on [${dependencies.joinToString()}]"
        
        override val name: String
            get() = when (type) {
                1    -> data.components[detail]?.name  //component
                2    -> data.ships[detail]?.name       //ship
                3    -> "fleet size"
                4    -> "ship attack"
                5    -> "ship defence"
                6    -> "star base attack"
                7    -> "star base defence"
                10   -> "price"
                11   -> "magnet"
                12   -> "loot"
                else -> null
            } ?: ""
        
        override fun databaseRawFileName() = name
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.technologyMap[id] = this
        }
    }
    
    data class ShipBuilderSettings(
            @Expose @SerializedName("ItemType") override val itemType: Int = 100,
            @Expose @SerializedName("DefaultWeightPerCell") val defaultWeightPerCell: Double = 20.0,
            @Expose @SerializedName("MinimumWeightPerCell") val minimumWeightPerCell: Double = 10.0,
            @Expose @SerializedName("HullRepairCooldown") val hullRepairCooldown: Double = 1.0,
            @Expose @SerializedName("ArmorPointsPerCell") val armorPointsPerCell: Double = 0.5,
            @Expose @SerializedName("BaseEnergyPoints") val baseEnergyPoints: Double = 1.0,
            @Expose @SerializedName("BaseEnergyRechargeRate") val baseEnergyRechargeRate: Double = 0.1,
            @Expose @SerializedName("EnergyRechargeCooldown") val energyRechargeCooldown: Double = 1.0,
            @Expose @SerializedName("ShieldRechargeCooldown") val shieldRechargeCooldown: Double = 4.0,
            @Expose @SerializedName("MaxVelocity") val maxVelocity: Double = 20.0,
            @Expose @SerializedName("MaxTurnRate") val maxTurnRate: Double = 20.0
    ) : EHDataFile() {
        override fun toString() = "$fileName ShipBuilderSettings"
        
        override fun databaseRawFileName() = "ShipBuilderSettings"
        
        override val id get() = 0
        
        override fun addToData(data: Data) {
            super.addToData(data)
            data.shipBuilderSettings = this
        }
        
        override val name get() = "ShipBuilderSettings"
    }
    
    override fun toString(): String {
        return components.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                devices.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                weapons.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                ammunition.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                droneBays.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                ships.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                satellites.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                shipBuilds.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                satelliteBuilds.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                technology.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                componentStats.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                componentModifications.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                technologyMap.values.sortedBy { it.id }.joinToString(separator = "\n") + "\n\n" +
                shipBuilderSettings.toString()
    }
    
    companion object {
        val nameFromItemType = hashMapOf(
                1 to "Component",
                2 to "Device",
                3 to "Weapon",
                4 to "Ammunition",
                5 to "DroneBay",
                6 to "Ship",
                7 to "Satellite",
                8 to "ShipBuild",
                9 to "SatelliteBuild",
                10 to "Technology",
                11 to "ComponentStats",
                12 to "ComponentModifications",
                13 to "TechnologyMap",
                100 to "ShipBuildSettings"
        )
        
        val classFromItemType = hashMapOf(
                1 to Component::class.java,
                2 to Device::class.java,
                3 to Weapon::class.java,
                4 to Ammunition::class.java,
                5 to DroneBay::class.java,
                6 to Ship::class.java,
                7 to Satellite::class.java,
                8 to ShipBuild::class.java,
                9 to SatelliteBuild::class.java,
                10 to Technology::class.java,
                11 to ComponentStats::class.java,
                12 to ComponentModification::class.java,
                13 to TechnologyMap::class.java,
                100 to ShipBuilderSettings::class.java
        )
    }
}
