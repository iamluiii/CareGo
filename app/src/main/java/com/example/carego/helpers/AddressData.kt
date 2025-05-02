package com.example.carego.helpers

object AddressData {
    const val PROVINCE = "Pampanga"

    val municipalities = listOf(
        "Apalit",
        "Arayat",
        "Bacolor",
        "Candaba",
        "Floridablanca",
        "Guagua",
        "Lubao",
        "Macabebe",
        "Magalang",
        "Masantol",
        "Mexico",
        "Minalin",
        "Porac",
        "San Luis",
        "San Simon",
        "San Fernando",
        "Angeles City",
        "Mabalacat"
    )

    val barangays = mapOf(
        "Apalit" to listOf(
            "Balucuc", "Calantipe", "Cansinala", "Capalangan", "Colgante", "Paligui",
            "Sampaloc", "San Juan", "San Vicente", "Sucad", "Sulipan", "Tabuyuc"
        ),

        "Arayat" to listOf(
            "Arenas", "Baliti", "Batasan", "Buensuceso", "Candating", "Cupang", "Gatiawin",
            "Guemasan", "Kaledian", "La Paz", "Lacmit", "Lacquios", "Mapalad", "Palinlang",
            "Paralaya", "Plazang Luma", "Poblacion", "San Agustin Norte", "San Agustin Sur",
            "San Antonio", "San Jose Mesulo", "San Juan Bano", "San Mateo", "San Nicolas",
            "San Roque Bitas", "Santa Ana", "Santo Niño Tabuan", "Suclayin", "Telapayong", "Turod"
        ),

        "Bacolor" to listOf(
            "Balas", "Cabambangan (Poblacion)", "Cabetican", "Calibutbut", "Concepcion",
            "Dolores", "Duat", "Macabacle", "Magliman", "Maliwalu", "Mesalipit", "Parulog",
            "Potrero", "San Antonio", "San Isidro", "San Jose", "San Vicente", "Santa Barbara",
            "Santa Ines", "Santa Maria", "Tinajero"
        ),

        "Candaba" to listOf(
            "Bahay Pare", "Bambang", "Barit", "Buas", "Cuayang Bugtong", "Dulong Ilog", "Gulap",
            "Lanang", "Lourdes", "Mandasig", "Mandili", "Mapaniqui", "Paligui", "Pangclara",
            "Paralaya", "Pasig", "Pescadores", "Poblacion", "Pulong Gubat", "Salapungan",
            "San Agustin", "San Francisco", "San Isidro", "San Jose", "San Pablo", "San Patricio",
            "San Roque", "Santa Cruz", "Santa Monica", "Santo Niño", "Talang", "Tenejero", "Vizal San Pablo"
        ),

        "Floridablanca" to listOf(
            "Anon", "Apalit", "Bodega", "Calantas", "Carmencita", "Consuelo", "Dampe",
            "Del Carmen", "Fortuna", "Gutad", "Mabical", "Maligaya", "Nabuclod", "Pabanlag",
            "Paguiruan", "Palmayo", "Pandaguirig", "Poblacion", "San Antonio", "San Isidro",
            "San Jose", "San Nicolas", "San Pedro", "San Ramon", "San Roque", "Santa Maria",
            "Solib", "Valdez", "Basa Air Base", "Mawacat", "Pabanlag Resettlement",
            "San Francisco", "San Vicente"
        ),
        "Guagua" to listOf(
            "Ascomo", "Bancal", "Betis", "Dila-Dila", "Jose Abad Santos", "Lambac", "Maquiapo",
            "Natividad", "Prado Siongco", "Pulungmasle", "Rizal", "San Agustin", "San Antonio",
            "San Isidro", "San Jose", "San Juan", "San Matias", "San Miguel", "San Nicolas 1st",
            "San Nicolas 2nd", "San Pablo", "San Pedro", "San Rafael", "San Roque", "San Vicente",
            "Santa Filomena", "Santa Ines", "Santa Ursula", "Santiago", "Santo Cristo", "Santo Niño"
        ),

        "Lubao" to listOf(
            "Balantacan", "Calangain", "Concepcion", "Del Carmen", "De La Paz", "Dolores",
            "Don Ignacio Dimson", "Lourdes", "Prado Siongco", "Remedios", "San Agustin", "San Antonio",
            "San Francisco", "San Isidro", "San Jose Apunan", "San Jose Gumi", "San Juan", "San Matias",
            "San Nicolas 1st", "San Nicolas 2nd", "San Pablo 1st", "San Pablo 2nd", "San Pedro Palcarangan",
            "San Pedro Saug", "San Rafael", "San Roque Arbol", "San Roque Dau", "San Vicente",
            "Santa Barbara", "Santa Catalina", "Santa Cruz", "Santa Lucia", "Santa Maria", "Santa Monica",
            "Santa Rita", "Santo Domingo", "Santo Niño", "Santo Tomas", "Santo Toribio", "Santiago",
            "Santo Cristo", "Santo Rosario", "Santo Niño Tabuan", "Tinajero"
        ),

        "Macabebe" to listOf(
            "Batasan", "Caduang Tete", "Candelaria", "Castuli", "Consuelo", "Dalan Betis", "Mataguiti",
            "San Esteban", "San Francisco", "San Gabriel", "San Isidro", "San Jose", "San Juan",
            "San Rafael", "San Roque", "San Vicente", "Santa Cruz", "Santa Lutgarda", "Santa Maria",
            "Santo Niño", "Saplad David", "Saplad Santos", "Tacasan", "Telacsan", "Telapayong"
        ),

        "Magalang" to listOf(
            "Ayala", "Balitucan", "Camias", "Dolores", "Escaler", "La Paz", "Navaling", "San Agustin",
            "San Antonio", "San Francisco", "San Isidro", "San Jose", "San Juan", "San Nicolas 1st",
            "San Nicolas 2nd", "San Pablo", "San Pedro 1st", "San Pedro 2nd", "San Roque", "San Vicente",
            "Santa Cruz", "Santa Lucia", "Santa Maria", "Santo Niño", "Santo Rosario", "Santo Tomas", "Turu"
        ),

        "Masantol" to listOf(
            "Alauli", "Bagang", "Bagang Pulung Gubat", "Bebe Anac", "Bebe Matua", "Bulacus", "Caingin",
            "Malauli", "Nigui", "Puti", "Sagrada Familia", "San Agustin", "San Isidro", "San Nicolas",
            "San Pedro", "San Vicente", "Santa Cruz", "Santa Lucia Matua", "Santa Lucia Paguiba",
            "Santa Monica", "Santo Niño", "Sapang Kawayan", "Sapi-an", "Sua", "Tubig Mal"
        ),
        "Mexico" to listOf(
            "Acli", "Anao", "Balas", "Buenavista", "Camuning", "Cawayan", "Concepcion", "Coronado",
            "Divisoria", "Dolores", "Dulong Malabon", "Eden", "Laguerta", "Laput", "Masamat", "Masangsang",
            "Nueva Victoria", "Pali", "Panipuan", "Parian", "San Antonio", "San Carlos", "San Jose Matulid",
            "San Juan", "San Lorenzo", "San Miguel", "San Nicolas", "San Pablo", "San Patricio", "San Rafael",
            "San Roque", "San Vicente", "Santa Cruz", "Santa Lucia", "Santa Maria", "Santo Domingo", "Tangle",
            "Tangle North", "Tangle South", "Tangle West"
        ),

        "Minalin" to listOf(
            "Bulac", "Dawe", "Lourdes", "Maniango", "San Francisco 1st", "San Francisco 2nd",
            "San Isidro", "San Jose", "San Nicolas", "San Pedro", "San Vicente", "Santa Catalina",
            "Santa Cruz", "Santa Maria", "Santo Domingo"
        ),

        "Porac" to listOf(
            "Babo Pangulo", "Babo Sacan", "Balubad", "Calzadang Bayu", "Camias", "Cangatba", "Diarbulan",
            "Dolores", "Hacienda Dolores", "Jalung", "Mancatian", "Manuali", "Mitla Proper", "Pias",
            "Planas", "Poblacion", "Pulong Santol", "Salu", "San Jose", "Santa Cruz", "Sapang Uwak",
            "Sapang Maisac", "Sindalan", "Villa Maria", "Villa Rosario", "Villa Dolores", "Yakal", "Zambales Gate"
        ),

        "San Luis" to listOf(
            "San Agustin", "San Carlos", "San Isidro", "San Jose", "San Juan", "San Nicolas",
            "San Roque", "Santa Catalina", "Santa Cruz", "Santa Lucia", "Santa Monica", "Santo Niño",
            "Santo Rosario", "Sapang Maisac"
        ),

        "San Simon" to listOf(
            "Concepcion", "De La Paz", "Dela Paz Norte", "Malabon-Kaingin", "San Agustin", "San Isidro",
            "San Jose", "San Juan", "San Miguel", "San Nicolas", "San Pablo Proper", "San Pedro", "San Vicente",
            "Santa Cruz", "Santa Monica", "Santa Rita", "Santo Niño", "Santo Rosario", "Santo Tomas", "Tagumbao"
        ),
        "San Fernando" to listOf(
            "Alasas", "Bulaon", "Calulut", "Dela Paz Norte", "Dela Paz Sur", "Del Carmen",
            "Del Pilar", "Del Rosario", "Dolores", "Juliana", "Lara", "Magliman", "Maimpis",
            "Malino", "Malpitic", "Pandaras", "Panipuan", "San Agustin", "San Felipe",
            "San Isidro", "San Jose", "San Juan", "San Nicolas", "San Pedro", "Santa Lucia",
            "Santa Teresita", "Santo Niño", "Sindalan", "Siran", "Telabastagan"
        ),

        "Angeles City" to listOf(
            "Agapito del Rosario", "Amsic", "Anunas", "Balibago", "Capaya", "Cutcut", "Cuayan",
            "Lourdes Northwest", "Lourdes Sur", "Lourdes Sur East", "Malabanias", "Margot", "Mining",
            "Pampang", "Pandan", "Pulungbulu", "Pulung Cacutud", "Pulung Maragul", "San Jose",
            "San Nicolas", "Santa Teresita", "Sapangbato", "Sto. Cristo", "Sto. Domingo", "Virgen Delos Remedios"
        ),

        "Mabalacat" to listOf(
            "Atlu Bola", "Bical", "Bical Norte", "Bundagul", "Cacutud", "Calumpang", "Camachiles",
            "Dapdap", "Dolores", "Duquit", "Lourdes", "Lakandula", "Mabiga", "Mamatitang", "Mangalit",
            "Mawaque", "Paralayunan", "San Francisco", "San Joaquin", "San Francisco",
            "Santa Ines", "Santa Maria", "Santo Rosario", "Tabun", "Umpucan"
        )
    )
}
