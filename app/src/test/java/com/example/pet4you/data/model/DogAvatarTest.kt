package com.example.pet4you.data.model

import org.junit.Assert.*
import org.junit.Test

class DogAvatarTest {

    @Test
    fun `breedPreset returns correct preset for Labrador`() {
        val avatar = breedPreset("Labrador Retriever")
        assertEquals(BodyShape.ROUND,    avatar.bodyShape)
        assertEquals(FurColor.GOLDEN,   avatar.furColor)
        assertEquals(EarType.FLOPPY,    avatar.earType)
        assertEquals(Accessory.COLLAR,  avatar.accessory)
    }

    @Test
    fun `breedPreset returns default DogAvatar for unknown breed`() {
        val avatar = breedPreset("Unknown Breed XYZ")
        assertEquals(DogAvatar(), avatar)
    }

    @Test
    fun `toMap serializes all fields as enum name strings`() {
        val avatar = DogAvatar(BodyShape.STOCKY, FurColor.BLACK, EarType.POINTY, EyeType.SLEEPY, TailType.STUB, Accessory.COLLAR)
        val map = avatar.toMap()
        assertEquals("STOCKY",  map["bodyShape"])
        assertEquals("BLACK",   map["furColor"])
        assertEquals("POINTY",  map["earType"])
        assertEquals("SLEEPY",  map["eyeType"])
        assertEquals("STUB",    map["tailType"])
        assertEquals("COLLAR",  map["accessory"])
    }

    @Test
    fun `toDogAvatar deserializes all fields`() {
        val map: Map<String, Any> = mapOf(
            "bodyShape" to "STOCKY", "furColor" to "BLACK",
            "earType"   to "POINTY", "eyeType"  to "SLEEPY",
            "tailType"  to "STUB",   "accessory" to "COLLAR"
        )
        val avatar = map.toDogAvatar()
        assertEquals(BodyShape.STOCKY,  avatar.bodyShape)
        assertEquals(FurColor.BLACK,    avatar.furColor)
        assertEquals(EarType.POINTY,    avatar.earType)
        assertEquals(EyeType.SLEEPY,    avatar.eyeType)
        assertEquals(TailType.STUB,     avatar.tailType)
        assertEquals(Accessory.COLLAR,  avatar.accessory)
    }

    @Test
    fun `toDogAvatar uses defaults for missing fields`() {
        val avatar = emptyMap<String, Any>().toDogAvatar()
        assertEquals(DogAvatar(), avatar)
    }

    @Test
    fun `toMap and toDogAvatar roundtrip preserves all values`() {
        val original = DogAvatar(BodyShape.SLIM, FurColor.GRAY, EarType.ROUND, EyeType.BIG, TailType.CURLY, Accessory.HAT)
        val roundtripped = (original.toMap() as Map<String, Any>).toDogAvatar()
        assertEquals(original, roundtripped)
    }

    @Test
    fun `toDogAvatar uses defaults for invalid enum string values`() {
        val map: Map<String, Any> = mapOf(
            "bodyShape" to "INVALID",
            "furColor"  to "UNKNOWN_COLOR",
            "tailType"  to "FLYING"
        )
        val avatar = map.toDogAvatar()
        assertEquals(BodyShape.ROUND,    avatar.bodyShape)
        assertEquals(FurColor.GOLDEN,    avatar.furColor)
        assertEquals(TailType.STRAIGHT,  avatar.tailType)
        // unspecified fields still get defaults
        assertEquals(EarType.FLOPPY,     avatar.earType)
    }
}
