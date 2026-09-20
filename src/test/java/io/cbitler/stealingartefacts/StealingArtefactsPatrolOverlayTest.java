package io.cbitler.stealingartefacts;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StealingArtefactsPatrolOverlayTest {
    private static final double TOLERANCE = 0.0001;

    static Stream<Arguments> rotations() {
        return Stream.of(
                Arguments.of(1024, 0, 0.0),
                Arguments.of(1536, 0, Math.PI / 2),
                Arguments.of(0, 0, -Math.PI),
                Arguments.of(512, 0, -Math.PI / 2),
                Arguments.of(1280, 0, Math.PI / 4),
                Arguments.of(1024, 4096, Math.PI / 2)
        );
    }

    @ParameterizedTest
    @MethodSource("rotations")
    void spriteRotationAccountsForOrientationAndCameraYaw(int orientation, int cameraYaw, double expected) {
        assertEquals(expected, StealingArtefactsPatrolOverlay.spriteRotation(orientation, cameraYaw), TOLERANCE);
    }
}
