package nl.blackstardlb.bois.services.models

enum class OutfitRank(val id: Int, val apiName: String) {
    Technocrat(1, "Technocrat") {
        override fun isApplicable(memberStats: OutfitMemberStats): Boolean {
            return false
        }
    },
    LightKeeper(2, "Light Keeper") {
        override fun isApplicable(memberStats: OutfitMemberStats): Boolean {
            return false
        }

    },
    Guardian(3, "Guardian") {
        override fun isApplicable(memberStats: OutfitMemberStats): Boolean {
            return false
        }

    },
    Justicar(4, "Justicar") {
        override fun isApplicable(memberStats: OutfitMemberStats): Boolean {
            // TODO find router players
            return Disciple.isApplicable(memberStats) && memberStats.killsPM >= 0.55 || Disciple.isApplicable(memberStats) && memberStats.medicPercentage >= 50
        }

    },
    Inquisitor(5, "Inquisitor") {
        override fun isApplicable(memberStats: OutfitMemberStats): Boolean {
            return Disciple.isApplicable(memberStats) && memberStats.killsPM >= 0.35
        }

    },
    Acolyte(6, "Acolyte") {
        override fun isApplicable(memberStats: OutfitMemberStats): Boolean {
            return Disciple.isApplicable(memberStats) && memberStats.killsPM >= 0.2
        }

    },
    Disciple(7, "Disciple") {
        override fun isApplicable(memberStats: OutfitMemberStats): Boolean {
            return TruthSeeker.isApplicable(memberStats) && (memberStats.br >= 50 || memberStats.prestigeLevel > 0)
        }

    },
    TruthSeeker(8, "Truth Seeker") {
        override fun isApplicable(memberStats: OutfitMemberStats): Boolean {
            return true
        }
    };
    abstract fun isApplicable(memberStats: OutfitMemberStats): Boolean
}