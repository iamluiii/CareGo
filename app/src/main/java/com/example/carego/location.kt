package com.example.carego

object LocationData {
    val municipalityBarangayMap: Map<String, List<String>> = mapOf(
        "Angeles City" to listOf(
            "Agapito del Rosario", "Amsic", "Anunas", "Balibago", "Capaya", "Claro M. Recto",
            "Cuayan", "Cutcut", "Cutud", "Lourdes North West", "Lourdes Sur", "Lourdes Sur East",
            "Malabanias", "Margot", "Mining", "Ninoy Aquino", "Pampang", "Pandan",
            "Pulung Bulu", "Pulung Cacutud", "Pulung Maragul", "Salapungan", "San Felipe",
            "San Jose", "San Nicolas", "Santa Teresita", "Santo Cristo", "Santo Domingo",
            "Santo Rosario", "Sapangbato", "Sta. Trinidad"
        ),
        "Apalit" to listOf(
            "Balucuc", "Calantipe", "Cansinala", "Capalangan", "Colgante", "Paligui",
            "Sampaloc", "San Juan", "San Vicente", "Sulipan", "Sucad", "Tabuyuc"
        ),
        "Arayat" to listOf(
            "Arenas", "Baliti", "Batasan", "Buensuseso", "Candating", "Gatiawin", "Guemasan",
            "La Paz", "Lacquios", "Lomboy", "Mangga-Cacutud", "Mapalad", "Matamo", "Palinlang",
            "Planas", "San Agustin Norte", "San Agustin Sur", "San Antonio", "San Jose Mesulo",
            "San Juan Bano", "San Mateo", "San Nicolas", "San Roque", "Santa Catalina", "Santo Niño",
            "Santo Rosario", "Suclayin", "Tabuan", "Camba", "Balucuc"
        ),
        "Bacolor" to listOf(
            "Balas", "Cabalantian", "Cabalungan", "Cabetican", "Concepcion", "Dolores",
            "Duat", "Magliman", "Maliwalu", "Mesalipit", "Parulog", "Potrero", "San Antonio",
            "San Isidro", "San Vicente", "Santa Barbara", "Santa Ines", "Talba", "Tinajero"
        ),
        "Candaba" to listOf(
            "Bahay Pare", "Bambang", "Barit", "Batang 1st", "Batang 2nd", "Cuayang Bugtong",
            "Dalayap", "Gulap", "Lanang", "Lourdes", "Mabuhay", "Magumbali", "Mandasig", "Mandili",
            "Mangga", "Mapaniqui", "Pagsanghan", "Paligui", "Paralaya", "Pasig", "Pescadores",
            "Pulung Gubat", "Salapungan", "San Agustin", "San Antonio", "San Francisco", "San Isidro",
            "San Joaquin", "San Jose", "San Luis", "San Miguel", "San Pablo", "San Patricio", "San Pedro",
            "Santa Lucia", "Santa Monica", "Santo Niño", "Tagulod", "Talang"
        ),
        "Floridablanca" to listOf(
            "Anon", "Apalit", "Basa Air Base", "Bodega", "Cabangcalan", "Calantas", "Carmencita",
            "Consuelo", "Dampe", "Del Carmen", "Fortuna", "Gutad", "Mabical", "Maligaya", "Nabuclod",
            "Paguiruan", "Palmayo", "Poblacion", "San Antonio", "San Isidro", "San Jose", "San Nicolas",
            "Santa Monica", "Solib", "Valdez"
        ),
        "Guagua" to listOf(
            "Ascomo", "Bancal", "Bancal Sinubli", "Bari", "Calante", "Dauc", "Donic", "Iponan",
            "Jose Abad Santos", "Lambac", "Magsaysay", "Maquiapo", "Natividad", "Pulungmasle", "Rizal",
            "San Antonio", "San Isidro", "San Jose", "San Juan", "San Matias", "San Miguel", "San Nicolas",
            "San Pablo", "San Pedro", "San Rafael", "San Roque", "San Vicente", "Santa Filomena", "Santa Ines",
            "Santa Ursula", "Santo Cristo", "Santo Niño", "Santo Tomas"
        ),
        "Lubao" to listOf(
            "Bancal", "Batasan", "Calangain", "Concepcion", "Del Carmen", "Don Ignacio Dimson", "Lourdes",
            "Remedios", "San Agustin", "San Antonio", "San Francisco", "San Isidro", "San Jose Apunan",
            "San Juan", "San Miguel", "San Nicolas I", "San Nicolas II", "San Pablo 1st", "San Pablo 2nd",
            "San Pedro Palcarangan", "San Roque Arbol", "San Vicente", "Santa Barbara", "Santa Catalina",
            "Santa Cruz", "Santa Lucia", "Santa Maria", "Santa Monica", "Santa Teresa 1st", "Santa Teresa 2nd",
            "Santo Domingo", "Santo Niño", "Santiago"
        ),
        "Mabalacat" to listOf(
            "Atlu-Bola", "Bical", "Bical-Baculud", "Calumpang", "Camachiles", "Dapdap", "Dolores", "Duquit",
            "Lakandula", "Mabiga", "Mangalit", "Mangalit-Cutud", "Marcos Village", "Mauswagon", "Paralayunan",
            "Poblacion", "San Francisco", "San Joaquin", "San Rafael", "Santa Ines", "Santa Maria", "Santo Rosario",
            "Sapang Bato", "Tabun"
        ),
        "Macabebe" to listOf(
            "Caduang Tete", "Canalate", "Castuli", "Consuelo", "Mataguiti", "San Esteban", "San Francisco",
            "San Gabriel", "San Isidro", "San Jose", "San Juan", "San Nicolas", "San Rafael", "San Roque",
            "San Vicente", "Santa Cruz", "Santa Lutgarda", "Santa Maria", "Santa Rita", "Santa Rosario", "Santo Niño"
        ),
        "Magalang" to listOf(
            "Buenavista", "Camias", "Dolores", "Escaler", "La Paz", "Navaling", "San Agustin", "San Antonio",
            "San Francisco", "San Ildefonso", "San Isidro", "San Jose", "San Miguel", "San Nicolas 1st",
            "San Nicolas 2nd", "San Pablo", "San Pedro 1st", "San Pedro 2nd", "San Roque", "Santa Cruz",
            "Santa Lucia", "Santo Niño", "Santo Rosario"
        ),
        "Masantol" to listOf(
            "Bagang", "Bebe Anac", "Bebe Matua", "Bulacus", "Caingin", "Cambasi", "Malauli", "Nigui", "Palimpe",
            "Puti", "San Agustin", "San Isidro Matua", "San Nicolas", "San Pedro", "Santa Cruz", "Santa Lucia Matua",
            "Santa Lucia Puti", "Santa Monica", "Santo Niño", "Sua", "Sagrada"
        ),
        "Mexico" to listOf(
            "Anao", "Arenas", "Cawayan", "Dolores", "Eden", "Laput", "Laug", "Malino", "Masamat", "Mexico",
            "Nueva Victoria", "Pandacaqui", "Panipuan", "Parian", "San Antonio", "San Carlos", "San Jose Malino",
            "San Jose Matulid", "San Lorenzo", "San Miguel", "San Nicolas", "San Patricio", "San Rafael",
            "San Roque", "San Vicente", "Santa Cruz", "Santa Maria", "Santo Domingo", "Santo Rosario", "Tangle"
        ),
        "Minalin" to listOf(
            "Bulac", "Dawe", "Lourdes", "Maniango", "San Francisco", "San Isidro", "San Nicolas", "San Pedro",
            "San Vicente", "Santa Catalina", "Santa Maria", "Santo Domingo", "Saplad David"
        ),
        "Porac" to listOf(
            "Babo Pangulo", "Babo Sacan", "Babo Tablac", "Balubad", "Camias", "Cangatba", "Diaz", "Dolores",
            "Jalung", "Mancatian", "Manuali", "Mitla Proper", "Model Community", "Pias", "Planas", "Poblacion",
            "Pulung Santol", "Sapang Uwak", "Sepung Bulaon", "Sinalang", "Villa Maria"
        ),
        "San Fernando" to listOf(
            "Alasas", "Bulaon", "Calulut", "Dela Paz Norte", "Dela Paz Sur", "Del Carmen", "Del Pilar", "Del Rosario",
            "Dolores", "Juliana", "Lara", "Lourdes", "Maimpis", "Malino", "Malpitic", "Pandaras", "San Agustin",
            "San Felipe", "San Isidro", "San Jose", "San Juan", "San Nicolas", "San Pedro", "Santa Lucia",
            "Santa Teresita", "Santo Niño", "Sindalan", "Telabastagan"
        ),
        "San Luis" to listOf(
            "San Agustin", "San Carlos", "San Isidro", "San Jose", "San Juan", "San Nicolas",
            "San Pedro", "San Roque", "San Vicente", "Santa Catalina", "Santa Cruz", "Santa Lucia", "Santo Tomas"
        ),
        "San Simon" to listOf(
            "Concepcion", "De La Paz", "San Agustin", "San Isidro", "San Jose", "San Juan", "San Miguel",
            "San Nicolas", "Santa Cruz", "Santa Monica", "Santa Rita", "Santo Domingo"
        ),
        "Santa Ana" to listOf(
            "San Agustin", "San Bartolome", "San Isidro", "San Joaquin", "San Jose", "San Juan", "San Nicolas",
            "San Pedro", "San Roque", "San Vicente", "Santa Lucia", "Santa Maria", "Santo Rosario"
        ),
        "Santa Rita" to listOf(
            "Becuran", "Dila Dila", "San Agustin", "San Basilio", "San Isidro", "San Jose", "San Juan", "San Matias",
            "San Miguel", "San Roque", "San Vicente", "Santa Monica", "Santiago"
        ),
        "Santo Tomas" to listOf(
            "Moras dela Paz", "Poblacion", "San Bartolome", "San Matias", "San Vicente", "Santo Rosario"
        ),
        "Sasmuan" to listOf(
            "Batang 1st", "Batang 2nd", "Camias", "Malusac", "Mataqui", "San Antonio", "San Nicolas 1st",
            "San Nicolas 2nd", "San Pedro", "Santa Lucia", "Santiago", "Sebitanan", "Sinaoangan", "Sucad"
        )
    )
}
