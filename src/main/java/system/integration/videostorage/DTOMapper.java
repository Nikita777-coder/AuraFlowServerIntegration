package system.integration.videostorage;

import org.mapstruct.Mapper;
import system.integration.videostorage.dto.KinescopeData;
import system.integration.videostorage.dto.KinescopeVideoData;

@Mapper(componentModel = "spring")
public interface DTOMapper {
    KinescopeData kinescopeVideoDataToKinescopeData(KinescopeVideoData kinescopeVideoData);
}
