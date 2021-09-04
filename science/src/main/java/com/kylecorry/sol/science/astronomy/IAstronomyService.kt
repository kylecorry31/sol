package com.kylecorry.sol.science.astronomy

import com.kylecorry.sol.science.astronomy.eclipse.IEclipseService
import com.kylecorry.sol.science.astronomy.meteors.IMeteorShowerService
import com.kylecorry.sol.science.shared.ISeasonService

// TODO: Move solar panel service out of this
interface IAstronomyService : IEclipseService, ISunService, IMoonService, ISolarPanelService,
    IMeteorShowerService, ISeasonService {
}