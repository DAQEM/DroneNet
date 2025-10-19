package com.daqem.irobot.config;

import com.daqem.irobot.IRobot;
import com.daqem.yamlconfig.api.config.ConfigExtension;
import com.daqem.yamlconfig.api.config.ConfigType;
import com.daqem.yamlconfig.api.config.IConfigBuilder;
import com.daqem.yamlconfig.api.config.entry.IConfigEntry;
import com.daqem.yamlconfig.impl.config.ConfigBuilder;

public class IRobotConfig {

    public static void init() {
    }

    // Robot General
    public static final IConfigEntry<Double> RECHARGE_THRESHOLD_PERCENTAGE;

    // Robot Attributes
    public static final IConfigEntry<Double> MAX_HEALTH;
    public static final IConfigEntry<Double> MOVEMENT_SPEED;
    public static final IConfigEntry<Double> ATTACK_DAMAGE;
    public static final IConfigEntry<Double> BLOCK_BREAK_SPEED;
    public static final IConfigEntry<Double> MINING_EFFICIENCY;
    public static final IConfigEntry<Double> SUBMERGED_MINING_SPEED;
    public static final IConfigEntry<Double> FOLLOW_RANGE;

    // Energy System
    public static final IConfigEntry<Double> MOVEMENT_ENERGY_COST_PER_METER;
    public static final IConfigEntry<Double> MINING_ENERGY_COST_PER_BLOCK;
    public static final IConfigEntry<Double> FARMING_ENERGY_COST_PER_HARVEST;
    public static final IConfigEntry<Double> FARMING_ENERGY_COST_PER_REPLANT;
    public static final IConfigEntry<Double> WOODCUTTING_ENERGY_COST_PER_LOG;
    public static final IConfigEntry<Double> WOODCUTTING_ENERGY_COST_PER_REPLANT;
    public static final IConfigEntry<Integer> REGENERATION_COOLDOWN_TICKS;
    public static final IConfigEntry<Double> REGENERATION_ENERGY_COST;
    public static final IConfigEntry<Float> REGENERATION_AMOUNT;

    // Batteries
    public static final IConfigEntry<Integer> SMALL_BATTERY_CAPACITY;
    public static final IConfigEntry<Integer> MEDIUM_BATTERY_CAPACITY;
    public static final IConfigEntry<Integer> LARGE_BATTERY_CAPACITY;

    // Modules
    public static final IConfigEntry<Double> SPEED_BOOST_MULTIPLIER;
    public static final IConfigEntry<Double> MINING_SPEED_BONUS;
    public static final IConfigEntry<Double> ATTACK_DAMAGE_BONUS;
    public static final IConfigEntry<Double> DURABILITY_HEALTH_BONUS;
    public static final IConfigEntry<Double> BATTERY_EFFICIENCY_MULTIPLIER;
    public static final IConfigEntry<Double> SOLAR_PANEL_ENERGY_PER_SECOND;

    // Blocks
    public static final IConfigEntry<Integer> ROBOT_STATION_CHARGE_RATE;

    // Behaviors
    public static final IConfigEntry<Integer> POI_SEARCH_RANGE;
    public static final IConfigEntry<Integer> MINE_BLOCK_TIMEOUT_TICKS;
    public static final IConfigEntry<Integer> MINE_BLOCK_RETRY_ATTEMPTS;
    public static final IConfigEntry<Integer> CUT_TREE_TIMEOUT_TICKS;
    public static final IConfigEntry<Integer> CUT_TREE_RETRY_ATTEMPTS;
    public static final IConfigEntry<Integer> HARVEST_CROP_DELAY_TICKS;
    public static final IConfigEntry<Float> FOLLOW_OWNER_SPEED;
    public static final IConfigEntry<Integer> FOLLOW_START_DISTANCE;
    public static final IConfigEntry<Integer> FOLLOW_STOP_DISTANCE;
    public static final IConfigEntry<Integer> PROTECT_OWNER_RANGE;
    public static final IConfigEntry<Integer> STROLL_IN_AREA_COOLDOWN_TICKS;

    static {
        IConfigBuilder config = new ConfigBuilder(IRobot.MOD_ID, "irobot-common", ConfigExtension.YAML, ConfigType.COMMON);

        config.push("robot_general");
        RECHARGE_THRESHOLD_PERCENTAGE = config.defineDouble("recharge_threshold_percentage", 0.2, 0.05, 1.0)
                .withComments("The percentage of total energy at which the robot will seek a charging station.");
        config.pop();

        config.push("robot_attributes");
        MAX_HEALTH = config.defineDouble("max_health", 20.0, 1.0, 1024.0).withComments("The base maximum health of a Mini Robot.");
        MOVEMENT_SPEED = config.defineDouble("movement_speed", 0.5, 0.1, 5.0).withComments("The base movement speed of a Mini Robot.");
        ATTACK_DAMAGE = config.defineDouble("attack_damage", 4.0, 0.0, 100.0).withComments("The base attack damage of a Mini Robot.");
        BLOCK_BREAK_SPEED = config.defineDouble("block_break_speed", 1.0, 0.1, 100.0).withComments("A multiplier for the robot's overall block breaking speed.");
        MINING_EFFICIENCY = config.defineDouble("mining_efficiency", 0.0, 0.0, 200.0).withComments("Additive bonus to block breaking speed, similar to the Efficiency enchantment.");
        SUBMERGED_MINING_SPEED = config.defineDouble("submerged_mining_speed", 0.2, 0.01, 1.0).withComments("The multiplier for mining speed when the robot's 'eyes' are in water.");
        FOLLOW_RANGE = config.defineDouble("follow_range", 16.0, 4.0, 128.0).withComments("The range at which the robot can follow its owner or track targets.");
        config.pop();

        config.push("energy_system");
        MOVEMENT_ENERGY_COST_PER_METER = config.defineDouble("movement_energy_cost_per_meter", 0.0625, 0.0, 10.0)
                .withComments("Energy consumed per meter traveled. Calculated from distance squared, so 1/16 = 0.0625.");
        MINING_ENERGY_COST_PER_BLOCK = config.defineDouble("mining_energy_cost_per_block", 5.0, 0.0, 100.0).withComments("Energy consumed for each block mined.");
        FARMING_ENERGY_COST_PER_HARVEST = config.defineDouble("farming_energy_cost_per_harvest", 1.0, 0.0, 100.0).withComments("Energy consumed for harvesting a crop.");
        FARMING_ENERGY_COST_PER_REPLANT = config.defineDouble("farming_energy_cost_per_replant", 1.0, 0.0, 100.0).withComments("Energy consumed for replanting a crop.");
        WOODCUTTING_ENERGY_COST_PER_LOG = config.defineDouble("woodcutting_energy_cost_per_log", 2.0, 0.0, 100.0).withComments("Energy consumed for each log block broken.");
        WOODCUTTING_ENERGY_COST_PER_REPLANT = config.defineDouble("woodcutting_energy_cost_per_replant", 1.0, 0.0, 100.0).withComments("Energy consumed for replanting a sapling.");
        REGENERATION_COOLDOWN_TICKS = config.defineInteger("regeneration_cooldown_ticks", 20, 1, 200).withComments("Ticks between each health regeneration pulse (20 ticks = 1 second).");
        REGENERATION_ENERGY_COST = config.defineDouble("regeneration_energy_cost", 10.0, 0.0, 100.0).withComments("Energy cost to regenerate health.");
        REGENERATION_AMOUNT = config.defineFloat("regeneration_amount", 1.0F, 0.1F, 20.0F).withComments("Amount of health regenerated per pulse (1.0 = half a heart).");
        config.pop();

        config.push("batteries");
        SMALL_BATTERY_CAPACITY = config.defineInteger("small_battery_capacity", 500, 10, 100000).withComments("Maximum energy capacity of the Small Battery.");
        MEDIUM_BATTERY_CAPACITY = config.defineInteger("medium_battery_capacity", 1500, 10, 100000).withComments("Maximum energy capacity of the Medium Battery.");
        LARGE_BATTERY_CAPACITY = config.defineInteger("large_battery_capacity", 3000, 10, 100000).withComments("Maximum energy capacity of the Large Battery.");
        config.pop();

        config.push("modules");
        SPEED_BOOST_MULTIPLIER = config.defineDouble("speed_boost_multiplier", 0.5, 0.0, 10.0).withComments("Movement speed multiplier from the Speed Boost Module (0.5 = +50% base speed).");
        MINING_SPEED_BONUS = config.defineDouble("mining_speed_bonus", 2.0, 0.0, 200.0).withComments("Additive mining efficiency bonus from the Mining Speed Module.");
        ATTACK_DAMAGE_BONUS = config.defineDouble("attack_damage_bonus", 4.0, 0.0, 100.0).withComments("Additive attack damage bonus from the Attack Damage Module.");
        DURABILITY_HEALTH_BONUS = config.defineDouble("durability_health_bonus", 20.0, 0.0, 1024.0).withComments("Additive max health bonus from the Durability Module (20.0 = 10 hearts).");
        BATTERY_EFFICIENCY_MULTIPLIER = config.defineDouble("battery_efficiency_multiplier", 0.75, 0.1, 1.0).withComments("Multiplier for all energy consumption with the Battery Efficiency Module (0.75 = 25% less energy used).");
        SOLAR_PANEL_ENERGY_PER_SECOND = config.defineDouble("solar_panel_energy_per_second", 2.0, 0.0, 100.0).withComments("Energy generated per second by the Solar Panel Module in direct sunlight.");
        config.pop();

        config.push("blocks");
        config.push("robot_station");
        ROBOT_STATION_CHARGE_RATE = config.defineInteger("charge_rate_per_tick", 1, 0, 1000).withComments("Energy restored per tick to a robot or battery item on the Robot Station.");
        config.pop();
        config.pop();

        config.push("behaviors");
        POI_SEARCH_RANGE = config.defineInteger("poi_search_range", 128, 16, 512).withComments("The maximum range (in blocks) a robot will search for a Point of Interest (like a charging station or dropoff chest).");

        config.push("mining");
        MINE_BLOCK_TIMEOUT_TICKS = config.defineInteger("mine_block_timeout_ticks", 600, 100, 72000).withComments("Maximum time in ticks a robot will spend trying to mine a single block before giving up (600 ticks = 30 seconds).");
        MINE_BLOCK_RETRY_ATTEMPTS = config.defineInteger("mine_block_retry_attempts", 5, 0, 20).withComments("How many times a robot will try to move closer to a block it wants to mine before giving up.");
        config.pop();

        config.push("woodcutting");
        CUT_TREE_TIMEOUT_TICKS = config.defineInteger("cut_tree_timeout_ticks", 6000, 100, 72000).withComments("Maximum time in ticks a robot will spend trying to cut down a single tree before giving up (6000 ticks = 5 minutes).");
        CUT_TREE_RETRY_ATTEMPTS = config.defineInteger("cut_tree_retry_attempts", 5, 0, 20).withComments("How many times a robot will try to move closer to a tree it wants to cut down before giving up.");
        config.pop();

        config.push("farming");
        HARVEST_CROP_DELAY_TICKS = config.defineInteger("harvest_crop_delay_ticks", 20, 1, 200).withComments("The delay in ticks after reaching a crop before the robot harvests it (20 ticks = 1 second).");
        config.pop();

        config.push("following_and_protecting");
        FOLLOW_OWNER_SPEED = config.defineFloat("follow_owner_speed", 0.75F, 0.1F, 5.0F).withComments("The speed modifier when the robot is following its owner.");
        FOLLOW_START_DISTANCE = config.defineInteger("follow_start_distance", 6, 2, 64).withComments("The distance (in blocks) from its owner at which the robot will start walking towards them.");
        FOLLOW_STOP_DISTANCE = config.defineInteger("follow_stop_distance", 4, 1, 32).withComments("The distance (in blocks) from its owner at which the robot will stop walking towards them.");
        PROTECT_OWNER_RANGE = config.defineInteger("protect_owner_range", 16, 2, 64).withComments("The range (in blocks) within which the robot will engage hostile mobs attacking its owner.");
        STROLL_IN_AREA_COOLDOWN_TICKS = config.defineInteger("stroll_in_area_cooldown_ticks", 100, 20, 1200).withComments("The cooldown in ticks between random strolls when in 'Protecting' mode.");
        config.pop();

        config.pop();


        config.build();
    }
}