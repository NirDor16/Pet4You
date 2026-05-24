package com.example.pet4you.repository

import com.example.pet4you.network.dogCeoApiService
import java.util.concurrent.ConcurrentHashMap

object DogCeoRepository {

    private val cache = ConcurrentHashMap<String, String>()

    suspend fun getBreedImageUrl(breedName: String): String? {
        cache[breedName]?.let { return it }
        val path = breedNameToDogCeoPath(breedName) ?: return null
        return runCatching { dogCeoApiService.getRandomBreedImage(path).imageUrl }
            .getOrNull()
            ?.also { url -> cache[breedName] = url }
    }

    suspend fun getRandomImageUrl(): String? =
        runCatching { dogCeoApiService.getRandomImage().imageUrl }.getOrNull()

    fun clearBreedCache(breedName: String) { cache.remove(breedName) }

    private fun breedNameToDogCeoPath(breedName: String): String? = when (breedName.lowercase()) {
        "affenpinscher"                  -> "affenpinscher"
        "afghan hound"                   -> "hound/afghan"
        "airedale terrier"               -> "terrier/airedale"
        "akita"                          -> "akita"
        "alaskan malamute"               -> "malamute"
        "american staffordshire terrier" -> "terrier/american"
        "australian shepherd"            -> "australian/shepherd"
        "basenji"                        -> "basenji"
        "basset hound"                   -> "hound/basset"
        "beagle"                         -> "beagle"
        "belgian malinois"               -> "malinois"
        "bernese mountain dog"           -> "mountain/bernese"
        "bichon frise"                   -> "frise/bichon"
        "bloodhound"                     -> "hound/blood"
        "border collie"                  -> "collie/border"
        "boston terrier"                 -> "terrier/boston"
        "boxer"                          -> "boxer"
        "bull terrier"                   -> "terrier/bull"
        "bulldog"                        -> "bulldog/english"
        "cairn terrier"                  -> "terrier/cairn"
        "cavalier king charles spaniel"  -> "spaniel/cocker"
        "chihuahua"                      -> "chihuahua"
        "chow chow"                      -> "chow"
        "cocker spaniel"                 -> "spaniel/cocker"
        "collie"                         -> "collie/border"
        "dachshund"                      -> "dachshund"
        "dalmatian"                      -> "dalmatian"
        "doberman pinscher"              -> "doberman"
        "english springer spaniel"       -> "spaniel/springer"
        "french bulldog"                 -> "bulldog/french"
        "german shepherd"                -> "germanshepherd"
        "german shorthaired pointer"     -> "pointer/german"
        "golden retriever"               -> "retriever/golden"
        "great dane"                     -> "dane/great"
        "great pyrenees"                 -> "pyrenees"
        "greyhound"                      -> "greyhound/italian"
        "havanese"                       -> "havanese"
        "irish setter"                   -> "setter/irish"
        "italian greyhound"              -> "greyhound/italian"
        "jack russell terrier"           -> "terrier/russell"
        "labrador retriever"             -> "labrador"
        "maltese"                        -> "maltese"
        "mastiff"                        -> "mastiff/english"
        "miniature schnauzer"            -> "schnauzer/miniature"
        "newfoundland"                   -> "newfoundland"
        "papillon"                       -> "papillon"
        "pomeranian"                     -> "pomeranian"
        "poodle"                         -> "poodle/standard"
        "pug"                            -> "pug"
        "rottweiler"                     -> "rottweiler"
        "saint bernard"                  -> "stbernard"
        "samoyed"                        -> "samoyed"
        "scottish terrier"               -> "terrier/scottish"
        "shetland sheepdog"              -> "sheepdog/shetland"
        "shiba inu"                      -> "shiba"
        "shih tzu"                       -> "shihtzu"
        "siberian husky"                 -> "husky"
        "staffordshire bull terrier"     -> "bullterrier/staffordshire"
        "vizsla"                         -> "vizsla"
        "weimaraner"                     -> "weimaraner"
        "west highland white terrier"    -> "terrier/westhighland"
        "whippet"                        -> "whippet"
        "yorkshire terrier"              -> "terrier/yorkshire"
        else                             -> null
    }
}
