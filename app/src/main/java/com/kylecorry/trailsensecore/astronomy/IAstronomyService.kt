package com.kylecorry.trailsensecore.astronomy

import com.kylecorry.trailsensecore.astronomy.eclipse.IEclipseService
import com.kylecorry.trailsensecore.time.ISeasonService

// TODO: Move solar panel service out of this
interface IAstronomyService : IEclipseService, ISunService, IMoonService, ISolarPanelService,
    IMeteorShowerService, ISeasonService {
}