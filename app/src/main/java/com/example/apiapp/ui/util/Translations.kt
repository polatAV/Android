package com.example.apiapp.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.apiapp.R

@Composable
fun translateSpecies(species: String): String {
    return when (species.lowercase()) {
        "human" -> stringResource(R.string.species_human)
        "alien" -> stringResource(R.string.species_alien)
        "humanoid" -> stringResource(R.string.species_humanoid)
        "poopybutthole" -> stringResource(R.string.species_poopybutthole)
        "mythological creature" -> stringResource(R.string.species_mythological_creature)
        "animal" -> stringResource(R.string.species_animal)
        "robot" -> stringResource(R.string.species_robot)
        "cronenberg" -> stringResource(R.string.species_cronenberg)
        "disease" -> stringResource(R.string.species_disease)
        "unknown" -> stringResource(R.string.status_filter_unknown)
        else -> species
    }
}

@Composable
fun translateGender(gender: String): String {
    return when (gender.lowercase()) {
        "male" -> stringResource(R.string.gender_male)
        "female" -> stringResource(R.string.gender_female)
        "genderless" -> stringResource(R.string.gender_genderless)
        "unknown" -> stringResource(R.string.status_filter_unknown)
        else -> gender
    }
}

@Composable
fun translateStatus(status: String): String {
    return when (status.lowercase()) {
        "alive" -> stringResource(R.string.status_alive)
        "dead" -> stringResource(R.string.status_dead)
        "unknown" -> stringResource(R.string.status_filter_unknown)
        else -> status
    }
}

// имена планет — имена собственные, переводим только "unknown"
@Composable
fun translateOrigin(origin: String): String {
    return if (origin.equals("unknown", ignoreCase = true)) {
        stringResource(R.string.status_filter_unknown)
    } else {
        origin
    }
}
