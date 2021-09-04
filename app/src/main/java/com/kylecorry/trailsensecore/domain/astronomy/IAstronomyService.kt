package com.kylecorry.trailsensecore.domain.astronomy

import com.kylecorry.trailsensecore.domain.astronomy.eclipse.IEclipseService
import com.kylecorry.trailsensecore.domain.time.ISeasonService

// TODO: Move solar panel service out of this
interface IAstronomyService : IEclipseService, ISunService, IMoonService, ISolarPanelService,
    IMeteorShowerService, ISeasonService {
}