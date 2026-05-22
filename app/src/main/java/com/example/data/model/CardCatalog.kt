package com.example.data.model

object CardCatalog {
    val cards = listOf(
        // === LENDÁRIA/ASSINADA/ANIMADA ===
        PlayerCard(
            id = 1,
            name = "Pelé",
            clubAndCountry = "Santos / Brasil",
            position = Position.ATA,
            overall = 99,
            stats = PlayerStats(pac = 97, sho = 99, pas = 95, dri = 98, def = 60, phy = 88),
            rarity = Rarity.ICON,
            initialHexColor = "#FFD700", // Bright Gold
            photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80"
        ),
        PlayerCard(
            id = 2,
            name = "Ronaldinho",
            clubAndCountry = "Barcelona / Brasil",
            position = Position.MEI,
            overall = 95,
            stats = PlayerStats(pac = 92, sho = 89, pas = 94, dri = 97, def = 35, phy = 79),
            rarity = Rarity.ICON,
            initialHexColor = "#DA70D6", // Orchid Purple
            photoUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=80"
        ),
        PlayerCard(
            id = 3,
            name = "Vini Jr",
            clubAndCountry = "R. Madrid / Brasil",
            position = Position.ATA,
            overall = 92,
            stats = PlayerStats(pac = 98, sho = 88, pas = 85, dri = 94, def = 42, phy = 80),
            rarity = Rarity.ANIMADA,
            initialHexColor = "#00FFFF", // Neon Electric Blue
            photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80"
        ),
        PlayerCard(
            id = 4,
            name = "Neymar Jr",
            clubAndCountry = "Al-Hilal / Brasil",
            position = Position.ATA,
            overall = 91,
            stats = PlayerStats(pac = 86, sho = 89, pas = 90, dri = 93, def = 38, phy = 65),
            rarity = Rarity.ASSINADA,
            initialHexColor = "#E6E6FA", // Lavender Metallic
            photoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&auto=format&fit=crop&q=80"
        ),
        PlayerCard(
            id = 5,
            name = "Kaká",
            clubAndCountry = "Milan / Brasil",
            position = Position.MEI,
            overall = 92,
            stats = PlayerStats(pac = 91, sho = 88, pas = 89, dri = 91, def = 45, phy = 78),
            rarity = Rarity.LENDARIA,
            initialHexColor = "#FFD700",
            photoUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=150&auto=format&fit=crop&q=80"
        ),

        // === ESPECIAL (Event / Promo) ===
        PlayerCard(
            id = 6,
            name = "Estêvão",
            clubAndCountry = "Palmeiras / Brasil",
            position = Position.ATA,
            overall = 86,
            stats = PlayerStats(pac = 92, sho = 82, pas = 80, dri = 89, def = 30, phy = 60),
            rarity = Rarity.ESPECIAL,
            initialHexColor = "#FF4500" // Coral Fire
        ),
        PlayerCard(
            id = 7,
            name = "Endrick",
            clubAndCountry = "R. Madrid / Brasil",
            position = Position.ATA,
            overall = 85,
            stats = PlayerStats(pac = 89, sho = 85, pas = 75, dri = 84, def = 38, phy = 83),
            rarity = Rarity.ESPECIAL,
            initialHexColor = "#FF4500"
        ),
        PlayerCard(
            id = 8,
            name = "Hulk",
            clubAndCountry = "Atlético-MG / Brasil",
            position = Position.ATA,
            overall = 83,
            stats = PlayerStats(pac = 78, sho = 86, pas = 78, dri = 81, def = 42, phy = 92),
            rarity = Rarity.ESPECIAL,
            initialHexColor = "#32CD32" // Lime Green
        ),

        // === OURO ===
        PlayerCard(
            id = 9,
            name = "Alisson",
            clubAndCountry = "Liverpool / Brasil",
            position = Position.GOL,
            overall = 89,
            stats = PlayerStats(pac = 86, sho = 85, pas = 85, dri = 89, def = 47, phy = 90),
            rarity = Rarity.OURO,
            initialHexColor = "#D4AF37" // Dark Goldenrod
        ),
        PlayerCard(
            id = 10,
            name = "Rodrygo",
            clubAndCountry = "R. Madrid / Brasil",
            position = Position.ATA,
            overall = 87,
            stats = PlayerStats(pac = 89, sho = 82, pas = 82, dri = 88, def = 43, phy = 64),
            rarity = Rarity.OURO,
            initialHexColor = "#D4AF37"
        ),
        PlayerCard(
            id = 11,
            name = "Marquinhos",
            clubAndCountry = "PSG / Brasil",
            position = Position.DFS,
            overall = 87,
            stats = PlayerStats(pac = 79, sho = 53, pas = 75, dri = 74, def = 89, phy = 80),
            rarity = Rarity.OURO,
            initialHexColor = "#D4AF37"
        ),
        PlayerCard(
            id = 12,
            name = "Raphinha",
            clubAndCountry = "Barcelona / Brasil",
            position = Position.ATA,
            overall = 86,
            stats = PlayerStats(pac = 91, sho = 81, pas = 81, dri = 86, def = 50, phy = 73),
            rarity = Rarity.OURO,
            initialHexColor = "#D4AF37"
        ),

        // === PRATA ===
        PlayerCard(
            id = 13,
            name = "Calleri",
            clubAndCountry = "São Paulo / Argentina",
            position = Position.ATA,
            overall = 80,
            stats = PlayerStats(pac = 72, sho = 82, pas = 64, dri = 70, def = 45, phy = 85),
            rarity = Rarity.PRATA,
            initialHexColor = "#C0C0C0" // Silver
        ),
        PlayerCard(
            id = 14,
            name = "Lucas Moura",
            clubAndCountry = "São Paulo / Brasil",
            position = Position.MEI,
            overall = 81,
            stats = PlayerStats(pac = 85, sho = 78, pas = 75, dri = 83, def = 48, phy = 70),
            rarity = Rarity.PRATA,
            initialHexColor = "#C0C0C0"
        ),
        PlayerCard(
            id = 15,
            name = "Raphael Veiga",
            clubAndCountry = "Palmeiras / Brasil",
            position = Position.MEI,
            overall = 82,
            stats = PlayerStats(pac = 74, sho = 82, pas = 81, dri = 79, def = 50, phy = 72),
            rarity = Rarity.PRATA,
            initialHexColor = "#C0C0C0"
        ),
        PlayerCard(
            id = 16,
            name = "Ganso",
            clubAndCountry = "Fluminense / Brasil",
            position = Position.MEI,
            overall = 76,
            stats = PlayerStats(pac = 48, sho = 73, pas = 85, dri = 78, def = 44, phy = 65),
            rarity = Rarity.PRATA,
            initialHexColor = "#C0C0C0"
        ),
        PlayerCard(
            id = 17,
            name = "Payet",
            clubAndCountry = "Vasco / França",
            position = Position.MEI,
            overall = 78,
            stats = PlayerStats(pac = 62, sho = 79, pas = 82, dri = 80, def = 35, phy = 68),
            rarity = Rarity.PRATA,
            initialHexColor = "#C0C0C0"
        ),

        // === BRONZE ===
        PlayerCard(
            id = 18,
            name = "Cano",
            clubAndCountry = "Fluminense / Argentina",
            position = Position.ATA,
            overall = 75,
            stats = PlayerStats(pac = 65, sho = 79, pas = 60, dri = 68, def = 30, phy = 72),
            rarity = Rarity.BRONZE,
            initialHexColor = "#CD7F32" // Bronze
        ),
        PlayerCard(
            id = 19,
            name = "Yuri Alberto",
            clubAndCountry = "Corinthians / Brasil",
            position = Position.ATA,
            overall = 74,
            stats = PlayerStats(pac = 81, sho = 73, pas = 63, dri = 72, def = 31, phy = 75),
            rarity = Rarity.BRONZE,
            initialHexColor = "#CD7F32"
        ),
        PlayerCard(
            id = 20,
            name = "Mastriani",
            clubAndCountry = "Athletico-PR / Uruguai",
            position = Position.ATA,
            overall = 73,
            stats = PlayerStats(pac = 68, sho = 74, pas = 58, dri = 67, def = 28, phy = 73),
            rarity = Rarity.BRONZE,
            initialHexColor = "#CD7F32"
        ),
        PlayerCard(
            id = 21,
            name = "Alerrandro",
            clubAndCountry = "Vitória / Brasil",
            position = Position.ATA,
            overall = 72,
            stats = PlayerStats(pac = 74, sho = 71, pas = 60, dri = 69, def = 25, phy = 70),
            rarity = Rarity.BRONZE,
            initialHexColor = "#CD7F32"
        ),
        PlayerCard(
            id = 22,
            name = "Matheus Pereira",
            clubAndCountry = "Cruzeiro / Brasil",
            position = Position.MEI,
            overall = 75,
            stats = PlayerStats(pac = 72, sho = 72, pas = 77, dri = 76, def = 40, phy = 66),
            rarity = Rarity.BRONZE,
            initialHexColor = "#CD7F32"
        ),
        // === NOVAS LENDAS NACIONAIS (ICONS) ===
        PlayerCard(
            id = 23,
            name = "Ronaldo Fenômeno",
            clubAndCountry = "Cruzeiro / Brasil",
            position = Position.ATA,
            overall = 98,
            stats = PlayerStats(pac = 97, sho = 96, pas = 80, dri = 95, def = 35, phy = 85),
            rarity = Rarity.ICON,
            initialHexColor = "#E5A3FF"
        ),
        PlayerCard(
            id = 24,
            name = "Zico",
            clubAndCountry = "Flamengo / Brasil",
            position = Position.MEI,
            overall = 97,
            stats = PlayerStats(pac = 89, sho = 95, pas = 97, dri = 94, def = 50, phy = 78),
            rarity = Rarity.ICON,
            initialHexColor = "#E5A3FF"
        ),
        PlayerCard(
            id = 25,
            name = "Romário",
            clubAndCountry = "Vasco / Brasil",
            position = Position.ATA,
            overall = 96,
            stats = PlayerStats(pac = 94, sho = 98, pas = 82, dri = 95, def = 32, phy = 80),
            rarity = Rarity.ICON,
            initialHexColor = "#E5A3FF"
        ),
        PlayerCard(
            id = 26,
            name = "Garrincha",
            clubAndCountry = "Botafogo / Brasil",
            position = Position.ATA,
            overall = 96,
            stats = PlayerStats(pac = 96, sho = 86, pas = 92, dri = 99, def = 38, phy = 68),
            rarity = Rarity.ICON,
            initialHexColor = "#E5A3FF"
        ),
        // === NOVAS ESTRELAS DO BRASILEIRÃO ===
        PlayerCard(
            id = 27,
            name = "Garro",
            clubAndCountry = "Corinthians / Argentina",
            position = Position.MEI,
            overall = 84,
            stats = PlayerStats(pac = 78, sho = 80, pas = 86, dri = 84, def = 55, phy = 72),
            rarity = Rarity.OURO,
            initialHexColor = "#D4AF37"
        ),
        PlayerCard(
            id = 28,
            name = "Arrascaeta",
            clubAndCountry = "Flamengo / Uruguai",
            position = Position.MEI,
            overall = 85,
            stats = PlayerStats(pac = 75, sho = 83, pas = 88, dri = 86, def = 45, phy = 68),
            rarity = Rarity.ESPECIAL,
            initialHexColor = "#FF4500"
        ),
        PlayerCard(
            id = 29,
            name = "Pedro",
            clubAndCountry = "Flamengo / Brasil",
            position = Position.ATA,
            overall = 83,
            stats = PlayerStats(pac = 74, sho = 86, pas = 72, dri = 78, def = 38, phy = 82),
            rarity = Rarity.OURO,
            initialHexColor = "#D4AF37"
        ),
        PlayerCard(
            id = 30,
            name = "Alan Patrick",
            clubAndCountry = "Internacional / Brasil",
            position = Position.MEI,
            overall = 82,
            stats = PlayerStats(pac = 72, sho = 80, pas = 85, dri = 83, def = 48, phy = 70),
            rarity = Rarity.PRATA,
            initialHexColor = "#C0C0C0"
        ),
        // === NOVAS ESTRELAS MUNDIAIS (WORLD STARS) ===
        PlayerCard(
            id = 31,
            name = "Messi",
            clubAndCountry = "Inter Miami / Argentina",
            position = Position.ATA,
            overall = 94,
            stats = PlayerStats(pac = 85, sho = 92, pas = 94, dri = 96, def = 35, phy = 64),
            rarity = Rarity.ESPECIAL,
            initialHexColor = "#FF4500",
            photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80"
        ),
        PlayerCard(
            id = 32,
            name = "CR7",
            clubAndCountry = "Al-Nassr / Portugal",
            position = Position.ATA,
            overall = 91,
            stats = PlayerStats(pac = 84, sho = 92, pas = 78, dri = 82, def = 34, phy = 78),
            rarity = Rarity.ESPECIAL,
            initialHexColor = "#FF4500",
            photoUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=80"
        ),
        PlayerCard(
            id = 33,
            name = "Haaland",
            clubAndCountry = "Man. City / Noruega",
            position = Position.ATA,
            overall = 91,
            stats = PlayerStats(pac = 89, sho = 93, pas = 66, dri = 80, def = 45, phy = 88),
            rarity = Rarity.ANIMADA,
            initialHexColor = "#00FFFF",
            photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80"
        ),
        PlayerCard(
            id = 34,
            name = "Mbappé",
            clubAndCountry = "R. Madrid / França",
            position = Position.ATA,
            overall = 92,
            stats = PlayerStats(pac = 97, sho = 90, pas = 80, dri = 92, def = 36, phy = 78),
            rarity = Rarity.ANIMADA,
            initialHexColor = "#00FFFF",
            photoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&auto=format&fit=crop&q=80"
        )
    )

    fun getCardById(id: Int): PlayerCard? = cards.find { it.id == id }
}
