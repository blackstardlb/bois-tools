package nl.blackstardlb.bois.data.models

object Constants {
    const val ALL_FILTER = "all"

    object Actions {
        const val SUBSCRIBE = "subscribe"
        const val UN_SUBSCRIBE = "clearSubscribe "
    }

    object Events {
        const val GAIN_EXPERIENCE = "GainExperience"
        const val VEHICLE_DESTROY = "VehicleDestroy"
        const val ITEM_ADDED = "ItemAdded"
        const val GAIN_EXPERIENCE_BY_ID = "GainExperience_experience_id_"
    }

    object Services {
        const val EVENT = "event"
    }

    object ExperienceSources {
        const val GENERIC_NPC_SPAWN = "1410"
        const val SUNDER_SPAWN = "233"
        const val DESTROYED_SUNDER = "68"
        const val HEAL_PLAYER = "4"
        const val REVIVE = "7"
        const val SQUAD_HEAL = "51"
        const val SQUAD_REVIVE = "53"
    }

    object MessageTypes {
        const val SERVICE_MESSAGE = "serviceMessage"
    }

    object Endpoints {
        const val OUTFIT_MEMBER = "outfit_member"
        const val OUTFIT = "outfit"
        const val CHARACTER = "character"
        const val CHARACTERS_STAT_HISTORY = "characters_stat_history"
        const val CHARACTERS_STAT = "characters_stat"
    }
    object Class {
        const val COMBAT_MEDIC = "4"
    }
}