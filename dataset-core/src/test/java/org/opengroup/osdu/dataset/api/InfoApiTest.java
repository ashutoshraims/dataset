package org.opengroup.osdu.dataset.api;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.info.VersionInfoBuilder;
import org.opengroup.osdu.core.common.model.info.VersionInfo;
import org.opengroup.osdu.dataset.controller.InfoController;


import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class InfoApiTest {

    @Mock
    private VersionInfoBuilder versionInfoBuilder;

    @InjectMocks
    InfoController infoApi;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void infoApiTest() throws IOException {

        VersionInfo response = mock(VersionInfo.class);
        response.setVersion("1.0.0");
        when(versionInfoBuilder.buildVersionInfo()).thenReturn(response);
        VersionInfo actualResponse = infoApi.info();
        assertEquals(response.getVersion(), actualResponse.getVersion());

    }

}
