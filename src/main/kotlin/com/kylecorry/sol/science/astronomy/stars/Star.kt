package com.kylecorry.sol.science.astronomy.stars

import com.kylecorry.sol.science.astronomy.units.EquatorialCoordinate

/**
 * This research has made use of the SIMBAD database,
 * operated at CDS, Strasbourg, France
 *
 * 2000,A&AS,143,9 , "The SIMBAD astronomical database", Wenger et al.
 *
 * https://doi.org/10.1051/aas:2000332
 */

// http://cdsportal.u-strasbg.fr/?target=Rigel
// https://en.wikipedia.org/wiki/List_of_brightest_stars
// https://en.wikipedia.org/wiki/List_of_stars_for_navigation

class Star internal constructor(
    val bayerDesignation: String,
    val name: String,
    internal val coordinate: EquatorialCoordinate,
    val magnitude: Float,
    val motion: ProperMotion,
    val colorIndexBV: Float
)

/**
 * Proper motion of a star in degrees per year
 */
data class ProperMotion(val declination: Double, val rightAscension: Double)

val STAR_CATALOG = listOf(
    Star(
        "Alpha Andromedae",
        "Alpheratz",
        EquatorialCoordinate(29.090431118059698, 2.0969161856756404),
        2.059999942779541f,
        ProperMotion(-4.54e-05, 3.818333333333334e-05),
        -0.1099998950958252f
    ),
    Star(
        "Beta Andromedae",
        "Mirach",
        EquatorialCoordinate(35.62055765052624, 17.43301617293042),
        2.049999952316284f,
        ProperMotion(-3.116666666666667e-05, 4.8861111111111114e-05),
        1.5699999332427979f
    ),
    Star(
        "Gamma 1 Andromedae",
        "Almach",
        EquatorialCoordinate(42.32972842352701, 30.97480120653972),
        2.0999999046325684f,
        ProperMotion(-1.3694444444444443e-05, 1.1755555555555556e-05),
        1.2000000476837158f
    ),
    Star(
        "Alpha Aquilae",
        "Altair",
        EquatorialCoordinate(8.868321196436963, 297.69582729638694),
        0.7599999904632568f,
        ProperMotion(0.000107025, 0.00014895277777777778),
        0.2200000286102295f
    ),
    Star(
        "Gamma Aquilae",
        "Tarazed",
        EquatorialCoordinate(10.6132587226325, 296.5649123092774),
        2.7200000286102295f,
        ProperMotion(-3.7611111111111114e-07, 4.5091666666666666e-06),
        1.5099999904632568f
    ),
    Star(
        "Zeta Aquilae",
        "Okab",
        EquatorialCoordinate(13.86347728111111, 286.35253339791666),
        2.990000009536743f,
        ProperMotion(-2.664138888888889e-05, -1.826388888888889e-06),
        0.009999990463256836f
    ),
    Star(
        "Alpha Aquarii",
        "Sadalmelik",
        EquatorialCoordinate(-0.3198509554241667, 331.4459814409479),
        2.940000057220459f,
        ProperMotion(-2.9027777777777776e-06, 5.163888888888889e-06),
        0.9600000381469727f
    ),
    Star(
        "Beta Aquarii",
        "Sadalsuud",
        EquatorialCoordinate(-5.5711748280636115, 322.88971698347876),
        2.890000104904175f,
        ProperMotion(-2.2675e-06, 5.3372222222222215e-06),
        0.8199999332427979f
    ),
    Star(
        "Alpha Arae",
        "Alpha Arae",
        EquatorialCoordinate(-49.87614501138889, 262.9603813675),
        2.950000047683716f,
        ProperMotion(-1.8672222222222222e-05, -9.241666666666668e-06),
        -0.1700000762939453f
    ),
    Star(
        "Beta Arae",
        "Beta Arae",
        EquatorialCoordinate(-55.529881687777774, 261.3249491888263),
        2.8499999046325684f,
        ProperMotion(-7.2019444444444444e-06, -2.2330555555555554e-06),
        1.4600000381469727f
    ),
    Star(
        "Alpha Arietis",
        "Hamal",
        EquatorialCoordinate(23.46241755020095, 31.79335709957655),
        2.009999990463257f,
        ProperMotion(-4.1133333333333335e-05, 5.2375000000000006e-05),
        1.1600000858306885f
    ),
    Star(
        "Beta Arietis",
        "Sheratan",
        EquatorialCoordinate(20.808031471916408, 28.66004578884514),
        2.6500000953674316f,
        ProperMotion(-3.0669444444444444e-05, 2.7427777777777777e-05),
        0.12999987602233887f
    ),
    Star(
        "Alpha Aurigae",
        "Capella",
        EquatorialCoordinate(45.99799146983673, 79.17232794433404),
        0.07999999821186066f,
        ProperMotion(-0.00011858055555555555, 2.0902777777777778e-05),
        0.7999999970197678f
    ),
    Star(
        "Beta Aurigae",
        "Menkalinan",
        EquatorialCoordinate(44.94743257194444, 89.88217886833333),
        1.899999976158142f,
        ProperMotion(-2.638888888888889e-07, -1.5677777777777776e-05),
        0.029999971389770508f
    ),
    Star(
        "Epsilon Aurigae",
        "Almaaz",
        EquatorialCoordinate(43.82331030981, 75.49222653615),
        2.990000009536743f,
        ProperMotion(-8.513888888888889e-07, 2.452777777777778e-07),
        0.5399999618530273f
    ),
    Star(
        "Iota Aurigae",
        "Hassaleh",
        EquatorialCoordinate(33.166093772077225, 74.24841676022209),
        2.690000057220459f,
        ProperMotion(-4.940555555555556e-06, 1.0602777777777778e-06),
        1.5299997329711914f
    ),
    Star(
        "Theta Aurigae",
        "Mahasim",
        EquatorialCoordinate(37.21258462923103, 89.93029217610834),
        2.619999885559082f,
        ProperMotion(-2.0497222222222225e-05, 1.2119444444444445e-05),
        -0.07999992370605469f
    ),
    Star(
        "Alpha Boötis",
        "Arcturus",
        EquatorialCoordinate(19.1824091615312, 213.915300294925),
        -0.05000000074505806f,
        ProperMotion(-0.0005555722222222222, -0.00030371944444444445),
        1.2299999482929707f
    ),
    Star(
        "Epsilon Boötis",
        "Izar",
        EquatorialCoordinate(27.074222319675556, 221.24673982578958),
        2.450000047683716f,
        ProperMotion(5.84e-06, -1.411611111111111e-05),
        1.1599998474121094f
    ),
    Star(
        "Eta Boötis",
        "Muphrid",
        EquatorialCoordinate(18.397720717229475, 208.67116216800866),
        2.680000066757202f,
        ProperMotion(-9.896944444444444e-05, -1.6930555555555558e-05),
        0.5699999332427979f
    ),
    Star(
        "Delta Capricorni",
        "Deneb Algedi",
        EquatorialCoordinate(-16.12728708527778, 326.76018433125),
        2.8299999237060547f,
        ProperMotion(-8.241666666666666e-05, 7.269444444444444e-05),
        0.28999996185302734f
    ),
    Star(
        "Alpha Carinae",
        "Canopus",
        EquatorialCoordinate(-52.69566138386201, 95.98795782918306),
        -0.7400000095367432f,
        ProperMotion(6.455555555555555e-06, 5.536111111111111e-06),
        0.15000003576278687f
    ),
    Star(
        "Beta Carinae",
        "Miaplacidus",
        EquatorialCoordinate(-69.71720759721548, 138.29990608310192),
        1.690000057220459f,
        ProperMotion(3.026388888888889e-05, -4.346388888888889e-05),
        0.0f
    ),
    Star(
        "Epsilon Carinae",
        "Avior",
        EquatorialCoordinate(-59.509484191917416, 125.62848024272952),
        1.8600000143051147f,
        ProperMotion(6.127777777777777e-06, -7.088888888888889e-06),
        1.2700001001358032f
    ),
    Star(
        "Iota Carinae",
        "Aspidiske",
        EquatorialCoordinate(-59.275232029166666, 139.27252857166667),
        2.259999990463257f,
        ProperMotion(3.3277777777777777e-06, -5.2388888888888885e-06),
        0.18000006675720215f
    ),
    Star(
        "Theta Carinae",
        "Theta Carinae",
        EquatorialCoordinate(-64.39445022111111, 160.73917486416664),
        2.759999990463257f,
        ProperMotion(3.3416666666666663e-06, -5.0999999999999995e-06),
        -0.2200000286102295f
    ),
    Star(
        "Upsilon Carinae",
        "Upsilon Carinae",
        EquatorialCoordinate(-65.07200742206864, 146.77550706492468),
        2.990000009536743f,
        ProperMotion(1.3083333333333332e-06, -3.197222222222222e-06),
        0.25999999046325684f
    ),
    Star(
        "Alpha Cassiopeiae",
        "Schedar",
        EquatorialCoordinate(56.537329217042775, 10.126846007691249),
        2.2300000190734863f,
        ProperMotion(-8.776388888888889e-06, 1.364611111111111e-05),
        1.1700000762939453f
    ),
    Star(
        "Beta Cassiopeiae",
        "Caph",
        EquatorialCoordinate(59.14978109800713, 2.2945215777878776),
        2.2699999809265137f,
        ProperMotion(-4.9936111111111113e-05, 0.00014541666666666666),
        0.3399999141693115f
    ),
    Star(
        "Delta Cassiopeiae",
        "Ruchbah",
        EquatorialCoordinate(60.23528402972222, 21.453964462083334),
        2.680000066757202f,
        ProperMotion(-1.3672222222222222e-05, 8.238055555555556e-05),
        0.12999987602233887f
    ),
    Star(
        "Epsilon Cassiopeiae",
        "Segin",
        EquatorialCoordinate(63.670100066329994, 28.5988920257),
        3.369999885559082f,
        ProperMotion(-5.1075e-06, 8.218611111111112e-06),
        -0.14999985694885254f
    ),
    Star(
        "Gamma Cassiopeiae",
        "Gamma Cassiopeiae",
        EquatorialCoordinate(60.71674002472222, 14.17721289375),
        2.390000104904175f,
        ProperMotion(-1.0888888888888889e-06, 6.991666666666667e-06),
        -0.10000014305114746f
    ),
    Star(
        "Alpha Centauri",
        "Rigil Kentaurus",
        EquatorialCoordinate(-60.83397222222223, 219.9020833333333),
        -0.10000000149011612f,
        ProperMotion(0.00019055555555555555, -0.0010022222222222223),
        0.5000000074505806f
    ),
    Star(
        "Beta Centauri",
        "Hadar",
        EquatorialCoordinate(-60.373035161541594, 210.95585562281522),
        0.5799999833106995f,
        ProperMotion(-6.433333333333333e-06, -9.241666666666668e-06),
        -0.23f
    ),
    Star(
        "Delta Centauri",
        "Delta Centauri",
        EquatorialCoordinate(-50.722427392777774, 182.08957348875),
        2.5199999809265137f,
        ProperMotion(-3.0930555555555557e-06, -1.1836944444444444e-05),
        -0.12999987602233887f
    ),
    Star(
        "Epsilon Centauri",
        "Epsilon Centauri",
        EquatorialCoordinate(-53.466391146088995, 204.97190723090242),
        2.299999952316284f,
        ProperMotion(-3.255555555555556e-06, -4.25e-06),
        -0.2200000286102295f
    ),
    Star(
        "Eta Centauri",
        "Eta Centauri",
        EquatorialCoordinate(-42.15782521722222, 218.8767673375),
        2.309999942779541f,
        ProperMotion(-9.088888888888888e-06, -9.647222222222221e-06),
        -0.19000005722045898f
    ),
    Star(
        "Gamma Centauri",
        "Gamma Centauri",
        EquatorialCoordinate(-48.959871515023295, 190.37933367081536),
        2.1700000762939453f,
        ProperMotion(1.6083333333333333e-06, -5.158888888888889e-05),
        -0.009999990463256836f
    ),
    Star(
        "Iota Centauri",
        "Kulou",
        EquatorialCoordinate(-36.712304581225, 200.14922006503957),
        2.7300000190734863f,
        ProperMotion(-2.2828055555555554e-05, -9.43375e-05),
        0.029999971389770508f
    ),
    Star(
        "Theta Centauri",
        "Menkent",
        EquatorialCoordinate(-36.369954742511496, 211.670614682339),
        2.049999952316284f,
        ProperMotion(-0.00014390555555555555, -0.00014459166666666665),
        0.9900000095367432f
    ),
    Star(
        "Zeta Centauri",
        "Leepwal",
        EquatorialCoordinate(-47.28837451101241, 208.88494019857197),
        2.549999952316284f,
        ProperMotion(-1.2375e-05, -1.593611111111111e-05),
        -0.2200000286102295f
    ),
    Star(
        "Alpha Cephei",
        "Alderamin",
        EquatorialCoordinate(62.5855744637194, 319.6448846972256),
        2.4600000381469727f,
        ProperMotion(1.3636111111111112e-05, 4.181944444444445e-05),
        0.2200000286102295f
    ),
    Star(
        "Alpha Ceti",
        "Menkar",
        EquatorialCoordinate(4.089738771805157, 45.56988780332224),
        2.5299999713897705f,
        ProperMotion(-2.134722222222222e-05, -2.891666666666667e-06),
        1.6400001049041748f
    ),
    Star(
        "Beta Ceti",
        "Diphda",
        EquatorialCoordinate(-17.98660631592891, 10.897378736003901),
        2.009999990463257f,
        ProperMotion(8.88611111111111e-06, 6.459722222222222e-05),
        1.0099999904632568f
    ),
    Star(
        "Alpha Canis Majoris",
        "Sirius",
        EquatorialCoordinate(-16.71611586111111, 101.28715533333335),
        -1.4600000381469727f,
        ProperMotion(-0.00033974166666666665, -0.00015166944444444444),
        0.0f
    ),
    Star(
        "Beta Canis Majoris",
        "Mirzam",
        EquatorialCoordinate(-17.955918708888888, 95.67493896958332),
        1.9700000286102295f,
        ProperMotion(-2.1666666666666667e-07, -8.972222222222223e-07),
        -0.24000000953674316f
    ),
    Star(
        "Delta Canis Majoris",
        "Wezen",
        EquatorialCoordinate(-26.39319957888889, 107.09785021416667),
        1.840000033378601f,
        ProperMotion(9.194444444444444e-07, -8.666666666666667e-07),
        0.6799999475479126f
    ),
    Star(
        "Epsilon Canis Majoris",
        "Adhara",
        EquatorialCoordinate(-28.972086157360806, 104.65645315148348),
        1.5f,
        ProperMotion(3.6944444444444447e-07, 9.000000000000001e-07),
        -0.21000003814697266f
    ),
    Star(
        "Eta Canis Majoris",
        "Aludra",
        EquatorialCoordinate(-29.303105508471724, 111.02375950100142),
        2.450000047683716f,
        ProperMotion(1.6138888888888887e-06, -1.15e-06),
        -0.08000016212463379f
    ),
    Star(
        "Zeta Canis Majoris",
        "Furud",
        EquatorialCoordinate(-30.063366733888888, 95.07830016583333),
        2.990000009536743f,
        ProperMotion(1.1194444444444444e-06, 2.0333333333333335e-06),
        -0.1700000762939453f
    ),
    Star(
        "Alpha Canis Minoris",
        "Procyon",
        EquatorialCoordinate(5.224987557059477, 114.82549790798149),
        0.3700000047683716f,
        ProperMotion(-0.000288, -0.00019849722222222224),
        0.42000001668930054f
    ),
    Star(
        "Beta Canis Minoris",
        "Gomeisa",
        EquatorialCoordinate(8.2893157625, 111.78767390541668),
        2.890000104904175f,
        ProperMotion(-1.063611111111111e-05, -1.4377777777777777e-05),
        -0.09000015258789062f
    ),
    Star(
        "Alpha Columbae",
        "Phact",
        EquatorialCoordinate(-34.07410971972223, 84.91225429958334),
        2.6500000953674316f,
        ProperMotion(-6.7625e-06, 4.6805555555555556e-07),
        -0.12000012397766113f
    ),
    Star(
        "Alpha Coronae Borealis",
        "Alphecca",
        EquatorialCoordinate(26.714685000024716, 233.67195203403162),
        2.240000009536743f,
        ProperMotion(-2.4364166666666666e-05, 3.303527777777778e-05),
        -0.019999980926513672f
    ),
    Star(
        "Alpha 1 Crux",
        "Acrux",
        EquatorialCoordinate(-63.09909166666667, 186.6495666666667),
        1.2799999713897705f,
        ProperMotion(-4.083333333333333e-06, -9.833333333333333e-06),
        -0.1799999475479126f
    ),
    Star(
        "Beta Crux",
        "Mimosa",
        EquatorialCoordinate(-59.68877199622606, 191.9302865619529),
        1.25f,
        ProperMotion(-4.4944444444444445e-06, -1.1936111111111111e-05),
        -0.23000001907348633f
    ),
    Star(
        "Delta Crux",
        "Imai",
        EquatorialCoordinate(-58.748924076838335, 183.78632699611498),
        2.752000093460083f,
        ProperMotion(-3.1377777777777774e-06, -1.0264166666666667e-05),
        -0.18400001525878906f
    ),
    Star(
        "Epsilon Crux",
        "Ginan",
        EquatorialCoordinate(-60.40114813057249, 185.3400338906829),
        3.569999933242798f,
        ProperMotion(2.5498611111111112e-05, -4.7561944444444445e-05),
        1.4100000858306885f
    ),
    Star(
        "Gamma Crux",
        "Gacrux",
        EquatorialCoordinate(-57.11321345705891, 187.79149837560794),
        1.6399999856948853f,
        ProperMotion(-7.363333333333333e-05, 7.841666666666666e-06),
        1.590000033378601f
    ),
    Star(
        "Beta Corvi",
        "Kraz",
        EquatorialCoordinate(-23.39676021620222, 188.59681217680622),
        2.640000104904175f,
        ProperMotion(-1.575111111111111e-05, 3.1000000000000005e-07),
        0.8799998760223389f
    ),
    Star(
        "Delta Corvi",
        "Algorab",
        EquatorialCoordinate(-16.515431261666667, 187.46606318416664),
        2.940000057220459f,
        ProperMotion(-3.866222222222222e-05, -5.813222222222222e-05),
        -0.04999995231628418f
    ),
    Star(
        "Epsilon Corvi",
        "Epsilon Corvi",
        EquatorialCoordinate(-22.6197672175, 182.53116909374998),
        2.9800000190734863f,
        ProperMotion(2.9547222222222223e-06, -2.000527777777778e-05),
        1.3400001525878906f
    ),
    Star(
        "Gamma Corvi",
        "Gienah",
        EquatorialCoordinate(-17.541930457603193, 183.9515450373768),
        2.5799999237060547f,
        ProperMotion(6.072222222222222e-06, -4.405833333333334e-05),
        -0.1099998950958252f
    ),
    Star(
        "Alpha 2 Canum Venaticorum",
        "Cor Caroli",
        EquatorialCoordinate(38.31837643625333, 194.00693995375286),
        2.880000114440918f,
        ProperMotion(1.52875e-05, -6.493777777777778e-05),
        -0.12000012397766113f
    ),
    Star(
        "Alpha Cygni",
        "Deneb",
        EquatorialCoordinate(45.280338806527574, 310.35797975307673),
        1.25f,
        ProperMotion(5.138888888888889e-07, 5.583333333333333e-07),
        0.09000003337860107f
    ),
    Star(
        "Delta Cygni",
        "Fawaris",
        EquatorialCoordinate(45.13081463141527, 296.24365384430797),
        2.869999885559082f,
        ProperMotion(1.3138333333333334e-05, 1.1630555555555554e-05),
        -0.019999980926513672f
    ),
    Star(
        "Epsilon Cygni",
        "Aljanah",
        EquatorialCoordinate(33.97032834364584, 311.55280115351087),
        2.4800000190734863f,
        ProperMotion(8.577416666666666e-05, 0.0001016538888888889),
        1.0399999618530273f
    ),
    Star(
        "Gamma Cygni",
        "Sadr",
        EquatorialCoordinate(40.25667915638889, 305.55709098208337),
        2.2300000190734863f,
        ProperMotion(-2.527777777777778e-07, 6.638888888888889e-07),
        0.6700000762939453f
    ),
    Star(
        "Beta Draconis",
        "Rastaban",
        EquatorialCoordinate(52.30138870861111, 262.60817373291667),
        2.809999942779541f,
        ProperMotion(3.411111111111111e-06, -4.413888888888889e-06),
        0.9800000190734863f
    ),
    Star(
        "Eta Draconis",
        "Athebyne",
        EquatorialCoordinate(61.51420976085001, 245.99785970199997),
        2.740000009536743f,
        ProperMotion(1.5859444444444445e-05, -4.553888888888888e-06),
        0.9100000858306885f
    ),
    Star(
        "Gamma Draconis",
        "Eltanin",
        EquatorialCoordinate(51.48889561763423, 269.1515411786243),
        2.2300000190734863f,
        ProperMotion(-6.330555555555555e-06, -2.3555555555555555e-06),
        1.5299999713897705f
    ),
    Star(
        "Alpha Eridani",
        "Achernar",
        EquatorialCoordinate(-57.236752805555554, 24.428522833333336),
        0.46000000834465027f,
        ProperMotion(-1.0622222222222223e-05, 2.4166666666666667e-05),
        -0.1599999964237213f
    ),
    Star(
        "Beta Eridani",
        "Cursa",
        EquatorialCoordinate(-5.086496983969999, 76.96239535462041),
        2.7899999618530273f,
        ProperMotion(-1.95825e-05, -2.4578055555555555e-05),
        0.13000011444091797f
    ),
    Star(
        "Gamma Eridani",
        "Zaurak",
        EquatorialCoordinate(-13.508514805984444, 59.50735758877376),
        2.940000057220459f,
        ProperMotion(-3.111277777777778e-05, 1.7285277777777778e-05),
        1.5999999046325684f
    ),
    Star(
        "Theta Eridani",
        "Acamar",
        EquatorialCoordinate(-40.30468122722222, 44.56531355666667),
        3.2f,
        ProperMotion(6.105555555555556e-06, -1.4691666666666667e-05),
        0.128f
    ),
    Star(
        "Alpha Geminorum",
        "Castor",
        EquatorialCoordinate(31.88828221646326, 113.64947163976585),
        1.5800000429153442f,
        ProperMotion(-4.0330555555555554e-05, -5.3180555555555555e-05),
        0.039999961853027344f
    ),
    Star(
        "Beta Geminorum",
        "Pollux",
        EquatorialCoordinate(28.02619889009357, 116.32895777437875),
        1.1399999856948853f,
        ProperMotion(-1.2722222222222221e-05, -0.00017404166666666666),
        1.0000001192092896f
    ),
    Star(
        "Epsilon Geminorum",
        "Mebsuta",
        EquatorialCoordinate(25.131127192367778, 100.98302979662667),
        2.9800000190734863f,
        ProperMotion(-3.272222222222222e-06, -1.3480555555555554e-06),
        1.4099998474121094f
    ),
    Star(
        "Gamma Geminorum",
        "Alhena",
        EquatorialCoordinate(16.399280426663772, 99.42796042942895),
        1.9199999570846558f,
        ProperMotion(-1.5266666666666667e-05, 3.836111111111111e-06),
        0.0f
    ),
    Star(
        "Mu Geminorum",
        "Tejat",
        EquatorialCoordinate(22.513582745904277, 95.74011192619564),
        2.869999885559082f,
        ProperMotion(-3.056388888888889e-05, 1.566388888888889e-05),
        1.640000343322754f
    ),
    Star(
        "Alpha Gruis",
        "Alnair",
        EquatorialCoordinate(-46.96097438191952, 332.05826969609114),
        1.7100000381469727f,
        ProperMotion(-4.096388888888889e-05, 3.5191666666666664e-05),
        -0.12999999523162842f
    ),
    Star(
        "Beta Gruis",
        "Tiaki",
        EquatorialCoordinate(-46.884576444824646, 340.66687612556166),
        2.109999895095825f,
        ProperMotion(-1.2166666666666667e-06, 3.754444444444444e-05),
        1.6200001239776611f
    ),
    Star(
        "Beta Herculis",
        "Kornephoros",
        EquatorialCoordinate(21.489610745563056, 247.5550013403688),
        2.7699999809265137f,
        ProperMotion(-4.130277777777778e-06, -2.7450555555555555e-05),
        0.9300000667572021f
    ),
    Star(
        "Zeta Herculis",
        "Zeta Herculis",
        EquatorialCoordinate(31.602718703799262, 250.32150433146228),
        2.799999952316284f,
        ProperMotion(9.507777777777777e-05, -0.0001282),
        0.630000114440918f
    ),
    Star(
        "Alpha Hydrae",
        "Alphard",
        EquatorialCoordinate(-8.658599531745583, 141.89684459585948),
        1.9700000286102295f,
        ProperMotion(9.547222222222222e-06, -4.230555555555556e-06),
        1.4500000476837158f
    ),
    Star(
        "Alpha Hydri",
        "Alpha Hydri",
        EquatorialCoordinate(-61.56982215319751, 29.69224409758834),
        2.8399999141693115f,
        ProperMotion(5.1194444444444445e-06, 8.04336111111111e-05),
        0.29000020027160645f
    ),
    Star(
        "Beta Hydri",
        "Beta Hydri",
        EquatorialCoordinate(-77.25424611998604, 6.4377931550222804),
        2.7899999618530273f,
        ProperMotion(9.002499999999999e-05, 0.0006165388888888888),
        0.6200001239776611f
    ),
    Star(
        "Alpha Leonis",
        "Regulus",
        EquatorialCoordinate(11.967208776100023, 152.09296243828146),
        1.399999976158142f,
        ProperMotion(1.5527777777777778e-06, -6.909166666666666e-05),
        -0.15999996662139893f
    ),
    Star(
        "Beta Leonis",
        "Denebola",
        EquatorialCoordinate(14.572058064829658, 177.26490975591017),
        2.130000114440918f,
        ProperMotion(-3.1852777777777777e-05, -0.00013824444444444445),
        0.08999991416931152f
    ),
    Star(
        "Delta Leonis",
        "Zosma",
        EquatorialCoordinate(20.523718139047475, 168.52708926588556),
        2.5299999713897705f,
        ProperMotion(-3.607777777777778e-05, 3.983888888888889e-05),
        0.15000009536743164f
    ),
    Star(
        "Epsilon Leonis",
        "Epsilon Leonis",
        EquatorialCoordinate(23.77425377711222, 146.46280673687792),
        2.9800000190734863f,
        ProperMotion(-2.9997222222222223e-06, -1.2820555555555557e-05),
        0.809999942779541f
    ),
    Star(
        "Gamma Leonis",
        "Algieba",
        EquatorialCoordinate(19.84148521911754, 154.99312733272745),
        2.369999885559082f,
        ProperMotion(-4.2855555555555554e-05, 8.452777777777778e-05),
        1.4200000762939453f
    ),
    Star(
        "Alpha Leporis",
        "Arneb",
        EquatorialCoordinate(-17.82228927222222, 83.18256716166667),
        2.569999933242798f,
        ProperMotion(3.2777777777777776e-07, 9.88888888888889e-07),
        0.20000004768371582f
    ),
    Star(
        "Beta Leporis",
        "Nihal",
        EquatorialCoordinate(-20.759443885748333, 82.06134536679916),
        2.8399999141693115f,
        ProperMotion(-2.323611111111111e-05, -1.5966666666666667e-06),
        0.820000171661377f
    ),
    Star(
        "Alpha 2 Librae",
        "Zubenelgenubi",
        EquatorialCoordinate(-16.041776519834446, 222.7196378915803),
        2.75f,
        ProperMotion(-1.9e-05, -2.935555555555556e-05),
        0.15000009536743164f
    ),
    Star(
        "Beta Librae",
        "Zubeneschamali",
        EquatorialCoordinate(-9.382914410334685, 229.25172424914246),
        2.619999885559082f,
        ProperMotion(-5.458333333333333e-06, -2.7249999999999998e-05),
        -0.1099998950958252f
    ),
    Star(
        "Alpha Lupi",
        "Uridim",
        EquatorialCoordinate(-47.38819874611111, 220.4823157775),
        2.2860000133514404f,
        ProperMotion(-6.5750000000000006e-06, -5.816666666666667e-06),
        -0.16000008583068848f
    ),
    Star(
        "Beta Lupi",
        "Beta Lupi",
        EquatorialCoordinate(-43.13396384896446, 224.63302234983436),
        2.680000066757202f,
        ProperMotion(-1.1063888888888889e-05, -9.938888888888888e-06),
        -0.2200000286102295f
    ),
    Star(
        "Gamma Lupi",
        "Gamma Lupi",
        EquatorialCoordinate(-41.16675686900692, 233.78520144771352),
        2.765000104904175f,
        ProperMotion(-7.063888888888889e-06, -4.338888888888889e-06),
        -0.1790001392364502f
    ),
    Star(
        "Alpha Lyrae",
        "Vega",
        EquatorialCoordinate(38.783688956244, 279.234734787025),
        0.029999999329447746f,
        ProperMotion(7.950833333333333e-05, 5.5816666666666664e-05),
        0.0f
    ),
    Star(
        "Alpha Muscae",
        "Alpha Muscae",
        EquatorialCoordinate(-69.1355647886111, 189.29590786916668),
        2.6489999294281006f,
        ProperMotion(-3.555555555555556e-06, -1.1166666666666668e-05),
        -0.16499996185302734f
    ),
    Star(
        "Alpha Ophiuchi",
        "Rasalhague",
        EquatorialCoordinate(12.560037391671425, 263.73362272030505),
        2.069999933242798f,
        ProperMotion(-6.154722222222222e-05, 3.001944444444444e-05),
        0.15000009536743164f
    ),
    Star(
        "Beta Ophiuchi",
        "Cebalrai",
        EquatorialCoordinate(4.567303668469166, 265.86813295741786),
        2.75f,
        ProperMotion(4.428611111111111e-05, -1.1609166666666667e-05),
        1.1800000667572021f
    ),
    Star(
        "Delta Ophiuchi",
        "Yed Prior",
        EquatorialCoordinate(-3.6943257851008338, 243.58641259114916),
        2.75f,
        ProperMotion(-4.012027777777778e-05, -1.2594444444444445e-05),
        1.5900001525878906f
    ),
    Star(
        "Eta Ophiuchi",
        "Sabik",
        EquatorialCoordinate(-15.724906641710527, 257.5945287106208),
        2.4200000762939453f,
        ProperMotion(2.7547222222222224e-05, 1.1147222222222223e-05),
        0.04999995231628418f
    ),
    Star(
        "Zeta Ophiuchi",
        "Zeta Ophiuchi",
        EquatorialCoordinate(-10.56708603964, 249.2897502448742),
        2.559999942779541f,
        ProperMotion(6.872777777777778e-06, 2.9069444444444445e-06),
        0.019999980926513672f
    ),
    Star(
        "Alpha Orionis",
        "Betelgeuse",
        EquatorialCoordinate(7.407063995272694, 88.79293899077537),
        0.41999998688697815f,
        ProperMotion(3.138888888888889e-06, 7.65e-06),
        1.8499999940395355f
    ),
    Star(
        "Beta Orionis",
        "Rigel",
        EquatorialCoordinate(-8.201638364722209, 78.63446706693006),
        0.12999999523162842f,
        ProperMotion(1.3888888888888888e-07, 3.638888888888889e-07),
        -0.0299999937415123f
    ),
    Star(
        "Delta Orionis",
        "Mintaka",
        EquatorialCoordinate(-0.29909510708333326, 83.00166705557675),
        2.4100000858306885f,
        ProperMotion(-1.9166666666666665e-07, 1.777777777777778e-07),
        -0.3900001049041748f
    ),
    Star(
        "Epsilon Orionis",
        "Alnilam",
        EquatorialCoordinate(-1.2019191358333312, 84.05338894077023),
        1.690000057220459f,
        ProperMotion(-2.1666666666666667e-07, 4e-07),
        -0.18000006675720215f
    ),
    Star(
        "Gamma Orionis",
        "Bellatrix",
        EquatorialCoordinate(6.3497032644440665, 81.28276355652378),
        1.6399999856948853f,
        ProperMotion(-3.577777777777778e-06, -2.2527777777777774e-06),
        -0.2200000286102295f
    ),
    Star(
        "Iota Orionis",
        "Hatysa",
        EquatorialCoordinate(-5.909888523506666, 83.85827580502625),
        2.7699999809265137f,
        ProperMotion(-4.7027777777777777e-07, -7.822222222222222e-07),
        -0.24000000953674316f
    ),
    Star(
        "Kappa Orionis",
        "Saiph",
        EquatorialCoordinate(-9.66960491861111, 86.93912016833333),
        2.059999942779541f,
        ProperMotion(-3.555555555555556e-07, 4.055555555555555e-07),
        -0.1799999475479126f
    ),
    Star(
        "Lambda Orionis",
        "Meissa",
        EquatorialCoordinate(9.934155874166667, 83.78449002103218),
        3.6600000858306885f,
        ProperMotion(-8.166666666666666e-07, -9.444444444444445e-08),
        -0.18000006675720215f
    ),
    Star(
        "Zeta Orionis",
        "Alnitak",
        EquatorialCoordinate(-1.9425735859722049, 85.18969442793068),
        1.7699999809265137f,
        ProperMotion(5.638888888888888e-07, 8.861111111111111e-07),
        -0.21000003814697266f
    ),
    Star(
        "Alpha Pavonis",
        "Peacock",
        EquatorialCoordinate(-56.73508972631689, 306.4119043651986),
        1.9179999828338623f,
        ProperMotion(-2.3894444444444442e-05, 1.916666666666667e-06),
        -0.12699997425079346f
    ),
    Star(
        "Alpha Pegasi",
        "Markab",
        EquatorialCoordinate(15.205267147927536, 346.19022269142596),
        2.4800000190734863f,
        ProperMotion(-1.1472222222222221e-05, 1.6777777777777776e-05),
        -0.039999961853027344f
    ),
    Star(
        "Beta Pegasi",
        "Scheat",
        EquatorialCoordinate(28.082787124606668, 345.9435727452067),
        2.4200000762939453f,
        ProperMotion(3.8036111111111116e-05, 5.2125e-05),
        1.6700000762939453f
    ),
    Star(
        "Epsilon Pegasi",
        "Enif",
        EquatorialCoordinate(9.875008653333333, 326.04648391416663),
        2.390000104904175f,
        ProperMotion(1.2222222222222222e-07, 7.477777777777779e-06),
        1.5199999809265137f
    ),
    Star(
        "Eta Pegasi",
        "Matar",
        EquatorialCoordinate(30.221214713719995, 340.75053685395),
        2.950000047683716f,
        ProperMotion(-5.2697222222222225e-06, 5.543611111111111e-06),
        0.8599998950958252f
    ),
    Star(
        "Gamma Pegasi",
        "Algenib",
        EquatorialCoordinate(15.18359842959389, 3.3089681209050004),
        2.8399999141693115f,
        ProperMotion(-2.9805555555555555e-06, 1.3666666666666667e-07),
        -0.23000001907348633f
    ),
    Star(
        "Alpha Persei",
        "Mirfak",
        EquatorialCoordinate(49.86117929305556, 51.08070871833333),
        1.7899999618530273f,
        ProperMotion(-7.286111111111111e-06, 6.597222222222222e-06),
        0.48000001907348633f
    ),
    Star(
        "Beta Persei",
        "Algol",
        EquatorialCoordinate(40.95564667027778, 47.04221855625),
        2.119999885559082f,
        ProperMotion(-4.6111111111111107e-07, 8.305555555555556e-07),
        -0.04999995231628418f
    ),
    Star(
        "Epsilon Persei",
        "Epsilon Persei",
        EquatorialCoordinate(40.01019978155972, 59.463497424225004),
        2.890000104904175f,
        ProperMotion(-6.5427777777777774e-06, 4.679166666666667e-06),
        -0.18000006675720215f
    ),
    Star(
        "Gamma Persei",
        "Gamma Persei",
        EquatorialCoordinate(53.50646183498722, 46.19925046367875),
        2.930000066757202f,
        ProperMotion(-2.412222222222222e-06, -3.942777777777778e-06),
        0.7000000476837158f
    ),
    Star(
        "Zeta Persei",
        "Zeta Persei",
        EquatorialCoordinate(31.88363368388889, 58.53301031291666),
        2.8499999046325684f,
        ProperMotion(-2.5391666666666668e-06, 2.0205555555555554e-06),
        0.12000012397766113f
    ),
    Star(
        "Alpha Phoenicis",
        "Ankaa",
        EquatorialCoordinate(-42.305987194396046, 6.5710475153919266),
        2.380000114440918f,
        ProperMotion(-9.897222222222223e-05, 6.473611111111111e-05),
        1.0899999141693115f
    ),
    Star(
        "Alpha Piscis Austrini",
        "Fomalhaut",
        EquatorialCoordinate(-29.622237033389442, 344.4126927211701),
        1.159999966621399f,
        ProperMotion(-4.574166666666666e-05, 9.1375e-05),
        0.09000003337860107f
    ),
    Star(
        "Pi Puppis",
        "Pi Puppis",
        EquatorialCoordinate(-37.09747346353528, 109.28565865855),
        2.700000047683716f,
        ProperMotion(1.811111111111111e-06, -3.2116666666666667e-06),
        1.6200001239776611f
    ),
    Star(
        "Rho Puppis",
        "Tureis",
        EquatorialCoordinate(-24.304324429444446, 121.88603676458334),
        2.809999942779541f,
        ProperMotion(1.3036111111111111e-05, -2.302388888888889e-05),
        0.43000006675720215f
    ),
    Star(
        "Tau Puppis",
        "Tau Puppis",
        EquatorialCoordinate(-50.61453869751139, 102.48403810537876),
        2.930000066757202f,
        ProperMotion(-1.932388888888889e-05, 9.060555555555555e-06),
        1.2000000476837158f
    ),
    Star(
        "Zeta Puppis",
        "Naos",
        EquatorialCoordinate(-40.003147798268934, 120.89603140977576),
        2.25f,
        ProperMotion(4.633333333333334e-06, -8.252777777777779e-06),
        -0.26999998092651367f
    ),
    Star(
        "Alpha Scorpii",
        "Antares",
        EquatorialCoordinate(-26.432002611950832, 247.3519154198264),
        0.9100000262260437f,
        ProperMotion(-6.4722222222222225e-06, -3.3638888888888887e-06),
        1.8399999737739563f
    ),
    Star(
        "Beta Scorpii",
        "Acrab",
        EquatorialCoordinate(-19.80538888888889, 241.35929166666665),
        2.5f,
        ProperMotion(-6.677777777777778e-06, -1.4444444444444445e-06),
        -0.06999993324279785f
    ),
    Star(
        "Delta Scorpii",
        "Dschubba",
        EquatorialCoordinate(-22.621706426005783, 240.08335534565887),
        2.319999933242798f,
        ProperMotion(-9.83611111111111e-06, -2.8361111111111113e-06),
        -0.11999988555908203f
    ),
    Star(
        "Epsilon Scorpii",
        "Larawag",
        EquatorialCoordinate(-34.29323159305555, 252.54087838625),
        2.2899999618530273f,
        ProperMotion(-7.110555555555556e-05, -0.00017079166666666667),
        1.1600000858306885f
    ),
    Star(
        "Iota 1 Scorpii",
        "Iota 1 Scorpii",
        EquatorialCoordinate(-40.12699736232806, 266.89616445956506),
        2.992000102996826f,
        ProperMotion(-1.498888888888889e-06, 1.388888888888889e-09),
        0.5f
    ),
    Star(
        "Kappa Scorpii",
        "Kappa Scorpii",
        EquatorialCoordinate(-39.02998307638737, 265.62198000035505),
        2.385999917984009f,
        ProperMotion(-7.094444444444444e-06, -1.6805555555555554e-06),
        -0.17499995231628418f
    ),
    Star(
        "Lambda Scorpii",
        "Shaula",
        EquatorialCoordinate(-37.10382355111976, 263.40216718438023),
        1.6299999952316284f,
        ProperMotion(-8.555555555555556e-06, -2.369444444444444e-06),
        -0.13999998569488525f
    ),
    Star(
        "Mu 1 Scorpii",
        "Xamidimura",
        EquatorialCoordinate(-38.04739946400001, 252.96761814529),
        2.9800000190734863f,
        ProperMotion(-5.0875e-06, -2.903055555555556e-06),
        -0.16000008583068848f
    ),
    Star(
        "Pi Scorpii",
        "Fang",
        EquatorialCoordinate(-26.114107945, 239.71297182416666),
        2.9100000858306885f,
        ProperMotion(-7.452777777777777e-06, -3.1722222222222223e-06),
        -0.20000004768371582f
    ),
    Star(
        "Sigma Scorpii",
        "Alniyat",
        EquatorialCoordinate(-25.592792076666665, 245.29714880583333),
        2.890000104904175f,
        ProperMotion(-4.5222222222222225e-06, -2.9444444444444445e-06),
        0.12999987602233887f
    ),
    Star(
        "Tau Scorpii",
        "Paikauhale",
        EquatorialCoordinate(-28.2160170875, 248.97063688749995),
        2.809999942779541f,
        ProperMotion(-6.341666666666666e-06, -2.7472222222222226e-06),
        -0.25f
    ),
    Star(
        "Theta Scorpii",
        "Sargas",
        EquatorialCoordinate(-42.99782799333082, 264.3297077207907),
        1.850000023841858f,
        ProperMotion(-8.666666666666667e-07, 1.538888888888889e-06),
        0.43999993801116943f
    ),
    Star(
        "Upsilon Scorpii",
        "Lesath",
        EquatorialCoordinate(-37.295813475277775, 262.6909880116666),
        2.6500000953674316f,
        ProperMotion(-8.358333333333333e-06, -6.583333333333333e-07),
        -0.1700000762939453f
    ),
    Star(
        "Alpha Serpentis",
        "Unukalhai",
        EquatorialCoordinate(6.425630220244722, 236.0669754565879),
        2.630000114440918f,
        ProperMotion(1.2652777777777777e-05, 3.7200555555555555e-05),
        1.1699998378753662f
    ),
    Star(
        "Delta Sagittarii",
        "Kaus Media",
        EquatorialCoordinate(-29.828102262591386, 275.24851205687213),
        2.6679999828338623f,
        ProperMotion(-7.600277777777778e-06, 9.225555555555556e-06),
        1.4009997844696045f
    ),
    Star(
        "Epsilon Sagittarii",
        "Kaus Australis",
        EquatorialCoordinate(-34.38461648586744, 276.0429933505963),
        1.809999942779541f,
        ProperMotion(-3.45e-05, -1.095e-05),
        0.010000109672546387f
    ),
    Star(
        "Gamma 2 Sagittarii",
        "Alnasl",
        EquatorialCoordinate(-30.424089849444446, 271.45203374499994),
        2.990000009536743f,
        ProperMotion(-5.690555555555556e-05, -1.3566388888888889e-05),
        1.0099999904632568f
    ),
    Star(
        "Lambda Sagittarii",
        "Kaus Borealis",
        EquatorialCoordinate(-25.421698496944444, 276.99266966041665),
        2.809999942779541f,
        ProperMotion(-5.14075e-05, -1.2820000000000001e-05),
        1.0399999618530273f
    ),
    Star(
        "Pi Sagittarii",
        "Albaldah",
        EquatorialCoordinate(-21.023613981944443, 287.440970525),
        2.880000114440918f,
        ProperMotion(-1.0125e-05, -3.777777777777778e-07),
        0.3399999141693115f
    ),
    Star(
        "Sigma Sagittarii",
        "Nunki",
        EquatorialCoordinate(-26.29672411476388, 283.81636040649323),
        2.066999912261963f,
        ProperMotion(-1.4841666666666667e-05, 4.205555555555556e-06),
        -0.14399993419647217f
    ),
    Star(
        "Zeta Sagittarii",
        "Ascella",
        EquatorialCoordinate(-29.88006330096877, 285.6530426563352),
        2.5899999141693115f,
        ProperMotion(5.8638888888888885e-06, 2.997222222222222e-06),
        0.10000014305114746f
    ),
    Star(
        "Alpha Tauri",
        "Aldebaran",
        EquatorialCoordinate(16.5093023507718, 68.9801627900154),
        0.8600000143051147f,
        ProperMotion(-5.248333333333333e-05, 1.7625e-05),
        1.540000081062317f
    ),
    Star(
        "Beta Tauri",
        "Elnath",
        EquatorialCoordinate(28.607451724998228, 81.57297133176498),
        1.649999976158142f,
        ProperMotion(-4.821666666666667e-05, 6.322222222222223e-06),
        -0.12999999523162842f
    ),
    Star(
        "Eta Tauri",
        "Alcyone",
        EquatorialCoordinate(24.105135651944444, 56.871152303749994),
        2.869999885559082f,
        ProperMotion(-1.2130555555555556e-05, 5.372222222222222e-06),
        -0.08999991416931152f
    ),
    Star(
        "Alpha Trianguli Australis",
        "Atria",
        EquatorialCoordinate(-69.02771184691984, 252.1662295073803),
        1.8799999952316284f,
        ProperMotion(-8.772222222222222e-06, 4.997222222222222e-06),
        1.4499999284744263f
    ),
    Star(
        "Beta Trianguli Australis",
        "Beta Trianguli Australis",
        EquatorialCoordinate(-63.43072653638888, 238.785675265),
        2.8499999046325684f,
        ProperMotion(-0.00011144194444444444, -5.204611111111111e-05),
        0.29000020027160645f
    ),
    Star(
        "Gamma Trianguli Australis",
        "Gamma Trianguli Australis",
        EquatorialCoordinate(-68.67954667374444, 229.7274220385625),
        2.890000104904175f,
        ProperMotion(-8.663055555555556e-06, -1.8282222222222224e-05),
        0.0f
    ),
    Star(
        "Alpha Tucanae",
        "Lang-Exster",
        EquatorialCoordinate(-60.259629544748336, 334.62546848872375),
        2.819999933242798f,
        ProperMotion(-9.1175e-06, -2.138888888888889e-05),
        1.3599998950958252f
    ),
    Star(
        "Alpha Ursae Majoris",
        "Dubhe",
        EquatorialCoordinate(61.751034687818226, 165.9319646738126),
        1.7899999618530273f,
        ProperMotion(-9.63888888888889e-06, -3.725277777777778e-05),
        1.0699999332427979f
    ),
    Star(
        "Beta Ursae Majoris",
        "Merak",
        EquatorialCoordinate(56.382433649496384, 165.46033229797294),
        2.369999885559082f,
        ProperMotion(8.990277777777778e-06, 2.2210833333333334e-05),
        -0.019999980926513672f
    ),
    Star(
        "Delta Ursae Majoris",
        "Megrez",
        EquatorialCoordinate(57.03261697773611, 183.85649936126705),
        3.319999933242798f,
        ProperMotion(2.261388888888889e-06, 2.887416666666667e-05),
        0.09000015258789062f
    ),
    Star(
        "Epsilon Ursae Majoris",
        "Alioth",
        EquatorialCoordinate(55.95982295694445, 193.5072899675),
        1.7699999809265137f,
        ProperMotion(-2.288888888888889e-06, 3.108611111111111e-05),
        -0.019999980926513672f
    ),
    Star(
        "Eta Ursae Majoris",
        "Alkaid",
        EquatorialCoordinate(49.31326672942533, 206.88515734206297),
        1.8600000143051147f,
        ProperMotion(-4.141666666666667e-06, -3.3658333333333336e-05),
        -0.19000005722045898f
    ),
    Star(
        "Gamma Ursae Majoris",
        "Phecda",
        EquatorialCoordinate(53.69475972916666, 178.45769715249997),
        2.440000057220459f,
        ProperMotion(3.0583333333333334e-06, 2.9911111111111115e-05),
        0.009999990463256836f
    ),
    Star(
        "Zeta 1 Ursae Majoris",
        "Mizar",
        EquatorialCoordinate(54.925359883302775, 200.98142616882203),
        2.2200000286102295f,
        ProperMotion(-6.3005555555555555e-06, 3.401861111111111e-05),
        0.05099987983703613f
    ),
    Star(
        "Alpha Ursae Minoris",
        "Polaris",
        EquatorialCoordinate(89.26410896994187, 37.954560670189856),
        2.0199999809265137f,
        ProperMotion(-3.2916666666666668e-06, 1.2355555555555555e-05),
        0.5999999046325684f
    ),
    Star(
        "Beta Ursae Minoris",
        "Kochab",
        EquatorialCoordinate(74.15550393675127, 222.67635749796767),
        2.0799999237060547f,
        ProperMotion(3.1722222222222223e-06, -9.058333333333333e-06),
        1.4700000286102295f
    ),
    Star(
        "Delta 1 Velorum",
        "Alsephina",
        EquatorialCoordinate(-54.70882101944444, 131.1759432625),
        1.95f,
        ProperMotion(-2.8916666666666664e-05, 8e-06),
        0.04f
    ),
    Star(
        "Gamma 2 Velorum",
        "Gamma 2 Velorum",
        EquatorialCoordinate(-47.336586329303486, 122.38312555977059),
        1.8300000429153442f,
        ProperMotion(2.897222222222222e-06, -1.6861111111111112e-06),
        -0.25f
    ),
    Star(
        "Kappa Velorum",
        "Markeb",
        EquatorialCoordinate(-55.01066713416667, 140.52840670833334),
        2.4730000495910645f,
        ProperMotion(3.2e-06, -3.1666666666666667e-06),
        -0.1490001678466797f
    ),
    Star(
        "Lambda Velorum",
        "Suhail",
        EquatorialCoordinate(-43.432590908860746, 136.99899113791412),
        2.2100000381469727f,
        ProperMotion(3.7555555555555553e-06, -6.669444444444445e-06),
        1.6499998569488525f
    ),
    Star(
        "Mu Velorum",
        "Mu Velorum",
        EquatorialCoordinate(-49.420259938983605, 161.69239624144333),
        2.690000057220459f,
        ProperMotion(-1.3560833333333334e-05, 1.9038055555555558e-05),
        0.8999998569488525f
    ),
    Star(
        "Alpha Virginis",
        "Spica",
        EquatorialCoordinate(-11.161319485111932, 201.2982473615632),
        0.9700000286102295f,
        ProperMotion(-8.519444444444444e-06, -1.1763888888888889e-05),
        -0.23000001907348633f
    ),
    Star(
        "Epsilon Virginis",
        "Vindemiatrix",
        EquatorialCoordinate(10.959148721740554, 195.5441542302233),
        2.7899999618530273f,
        ProperMotion(5.50111111111111e-06, -7.605361111111111e-05),
        0.9200000762939453f
    ),
    Star(
        "Gamma Virginis",
        "Porrima",
        EquatorialCoordinate(-1.4494039524575002, 190.41511762006294),
        2.740000009536743f,
        ProperMotion(2.3805833333333332e-05, -0.00016810444444444447),
        0.3599998950958252f
    )
)

internal val STAR_CATALOG_BY_NAME = STAR_CATALOG.associateBy { it.name }
