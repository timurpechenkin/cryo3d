package io.github.timurpechenkin.casefile.resolve;

import io.github.timurpechenkin.casefile.dto.recording.ProfileDto;
import io.github.timurpechenkin.domain.grid.Grid3D;
import io.github.timurpechenkin.domain.recording.Profile;

public interface ProfileDiscretizer {

    Profile discretize(Grid3D grid3d, ProfileDto profileDto);
}
