package com.example.pet4you.data.model

import androidx.compose.ui.graphics.Color

enum class BodyShape { ROUND, STOCKY, SLIM }
enum class FurColor  { GOLDEN, BLACK, WHITE, BROWN, GRAY, SPOTTED }
enum class EarType   { FLOPPY, POINTY, ROUND }
enum class EyeType   { NORMAL, BIG, SLEEPY }
enum class TailType  { STRAIGHT, CURLY, STUB }
enum class Accessory { NONE, COLLAR, BOW, HAT, BANDANA }

data class DogAvatar(
    val bodyShape: BodyShape = BodyShape.ROUND,
    val furColor:  FurColor  = FurColor.GOLDEN,
    val earType:   EarType   = EarType.FLOPPY,
    val eyeType:   EyeType   = EyeType.NORMAL,
    val tailType:  TailType  = TailType.STRAIGHT,
    val accessory: Accessory = Accessory.NONE
)

fun FurColor.toColor(): Color = when (this) {
    FurColor.GOLDEN  -> Color(0xFFD4A017)
    FurColor.BLACK   -> Color(0xFF2C2C2C)
    FurColor.WHITE   -> Color(0xFFF0EDE8)
    FurColor.BROWN   -> Color(0xFF8B5E3C)
    FurColor.GRAY    -> Color(0xFF8B8B8B)
    FurColor.SPOTTED -> Color(0xFFF0EDE8)
}

fun DogAvatar.toMap(): Map<String, String> = mapOf(
    "bodyShape" to bodyShape.name,
    "furColor"  to furColor.name,
    "earType"   to earType.name,
    "eyeType"   to eyeType.name,
    "tailType"  to tailType.name,
    "accessory" to accessory.name
)

fun Map<String, Any>.toDogAvatar() = DogAvatar(
    bodyShape = BodyShape.valueOf((this["bodyShape"] as? String) ?: "ROUND"),
    furColor  = FurColor.valueOf((this["furColor"]  as? String) ?: "GOLDEN"),
    earType   = EarType.valueOf((this["earType"]   as? String) ?: "FLOPPY"),
    eyeType   = EyeType.valueOf((this["eyeType"]   as? String) ?: "NORMAL"),
    tailType  = TailType.valueOf((this["tailType"]  as? String) ?: "STRAIGHT"),
    accessory = Accessory.valueOf((this["accessory"] as? String) ?: "NONE")
)

val BREED_PRESETS: Map<String, DogAvatar> = mapOf(
    "Labrador Retriever"  to DogAvatar(BodyShape.ROUND,  FurColor.GOLDEN,  EarType.FLOPPY,  EyeType.NORMAL, TailType.STRAIGHT, Accessory.COLLAR),
    "Golden Retriever"    to DogAvatar(BodyShape.ROUND,  FurColor.GOLDEN,  EarType.FLOPPY,  EyeType.BIG,    TailType.STRAIGHT, Accessory.NONE),
    "Siberian Husky"      to DogAvatar(BodyShape.STOCKY, FurColor.GRAY,    EarType.POINTY,  EyeType.NORMAL, TailType.CURLY,    Accessory.NONE),
    "German Shepherd"     to DogAvatar(BodyShape.STOCKY, FurColor.BROWN,   EarType.POINTY,  EyeType.NORMAL, TailType.STRAIGHT, Accessory.COLLAR),
    "Poodle"              to DogAvatar(BodyShape.ROUND,  FurColor.WHITE,   EarType.ROUND,   EyeType.BIG,    TailType.CURLY,    Accessory.BOW),
    "Chihuahua"           to DogAvatar(BodyShape.SLIM,   FurColor.BROWN,   EarType.POINTY,  EyeType.BIG,    TailType.STRAIGHT, Accessory.NONE),
    "Rottweiler"          to DogAvatar(BodyShape.STOCKY, FurColor.BLACK,   EarType.FLOPPY,  EyeType.SLEEPY, TailType.STUB,     Accessory.COLLAR),
    "Beagle"              to DogAvatar(BodyShape.ROUND,  FurColor.SPOTTED, EarType.FLOPPY,  EyeType.NORMAL, TailType.STRAIGHT, Accessory.NONE),
    "Shih Tzu"            to DogAvatar(BodyShape.ROUND,  FurColor.WHITE,   EarType.ROUND,   EyeType.BIG,    TailType.CURLY,    Accessory.BOW),
    "French Bulldog"      to DogAvatar(BodyShape.STOCKY, FurColor.GRAY,    EarType.POINTY,  EyeType.BIG,    TailType.STUB,     Accessory.BANDANA),
    "Bulldog"             to DogAvatar(BodyShape.STOCKY, FurColor.SPOTTED, EarType.FLOPPY,  EyeType.SLEEPY, TailType.STUB,     Accessory.NONE),
    "Dachshund"           to DogAvatar(BodyShape.SLIM,   FurColor.BROWN,   EarType.FLOPPY,  EyeType.NORMAL, TailType.STRAIGHT, Accessory.NONE),
    "Dalmatian"           to DogAvatar(BodyShape.SLIM,   FurColor.SPOTTED, EarType.FLOPPY,  EyeType.NORMAL, TailType.STRAIGHT, Accessory.NONE),
    "Pomeranian"          to DogAvatar(BodyShape.ROUND,  FurColor.GOLDEN,  EarType.POINTY,  EyeType.BIG,    TailType.CURLY,    Accessory.NONE),
    "Maltese"             to DogAvatar(BodyShape.ROUND,  FurColor.WHITE,   EarType.ROUND,   EyeType.BIG,    TailType.STRAIGHT, Accessory.BOW),
    "Yorkshire Terrier"   to DogAvatar(BodyShape.SLIM,   FurColor.BROWN,   EarType.POINTY,  EyeType.BIG,    TailType.STRAIGHT, Accessory.BOW),
    "Boxer"               to DogAvatar(BodyShape.STOCKY, FurColor.BROWN,   EarType.FLOPPY,  EyeType.NORMAL, TailType.STUB,     Accessory.COLLAR),
    "Doberman Pinscher"   to DogAvatar(BodyShape.SLIM,   FurColor.BLACK,   EarType.POINTY,  EyeType.NORMAL, TailType.STUB,     Accessory.COLLAR),
    "Samoyed"             to DogAvatar(BodyShape.ROUND,  FurColor.WHITE,   EarType.ROUND,   EyeType.NORMAL, TailType.CURLY,    Accessory.NONE),
    "Pug"                 to DogAvatar(BodyShape.ROUND,  FurColor.BROWN,   EarType.ROUND,   EyeType.BIG,    TailType.CURLY,    Accessory.NONE),
    "Chow Chow"           to DogAvatar(BodyShape.STOCKY, FurColor.GOLDEN,  EarType.ROUND,   EyeType.SLEEPY, TailType.CURLY,    Accessory.NONE),
    "Border Collie"       to DogAvatar(BodyShape.SLIM,   FurColor.BLACK,   EarType.FLOPPY,  EyeType.NORMAL, TailType.STRAIGHT, Accessory.NONE),
    "Shiba Inu"           to DogAvatar(BodyShape.ROUND,  FurColor.GOLDEN,  EarType.POINTY,  EyeType.NORMAL, TailType.CURLY,    Accessory.NONE),
    "Cocker Spaniel"      to DogAvatar(BodyShape.ROUND,  FurColor.GOLDEN,  EarType.FLOPPY,  EyeType.BIG,    TailType.STUB,     Accessory.BOW),
    "Basset Hound"        to DogAvatar(BodyShape.STOCKY, FurColor.SPOTTED, EarType.FLOPPY,  EyeType.SLEEPY, TailType.STRAIGHT, Accessory.NONE),
)

fun breedPreset(breed: String): DogAvatar = BREED_PRESETS[breed] ?: DogAvatar()
