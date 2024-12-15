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
enum class Star(
    internal val coordinate: EquatorialCoordinate,
    val magnitude: Float,
    val motion: ProperMotion,
    val colorIndexBV: Float
) {
    Sirius(EquatorialCoordinate(-16.716115833333333, 101.28715499999998), -1.4600000381469727f, ProperMotion(-0.00033974166666666665, -0.00015166944444444444), 0.0f),
    Canopus(EquatorialCoordinate(-52.69566138888889, 95.9879575), -0.7400000095367432f, ProperMotion(6.455555555555555e-06, 5.536111111111111e-06), 0.15000003576278687f),
    RigilKentaurus(EquatorialCoordinate(-60.8339925, 219.9020583333333), 0.009999999776482582f, ProperMotion(0.000131575, -0.001022013888888889), 0.7100000381469727f),
    Arcturus(EquatorialCoordinate(19.18240916666667, 213.91529999999997), -0.05000000074505806f, ProperMotion(-0.0005555722222222222, -0.00030371944444444445), 1.2299998998641968f),
    Vega(EquatorialCoordinate(38.78368888888889, 279.23473458333336), 0.029999999329447746f, ProperMotion(7.950833333333333e-05, 5.5816666666666664e-05), 0.0f),
    Rigel(EquatorialCoordinate(-8.201638333333333, 78.63446708333332), 0.12999999523162842f, ProperMotion(1.3888888888888888e-07, 3.638888888888889e-07), -0.0299999937415123f),
    Procyon(EquatorialCoordinate(5.2249875, 114.82549791666666), 0.3700000047683716f, ProperMotion(-0.000288, -0.00019849722222222224), 0.42000001668930054f),
    Achernar(EquatorialCoordinate(-57.23675277777778, 24.428522499999996), 0.46000000834465027f, ProperMotion(-1.0622222222222223e-05, 2.4166666666666667e-05), -0.1599999964237213f),
    Betelgeuse(EquatorialCoordinate(7.40706388888889, 88.79293874999999), 0.41999998688697815f, ProperMotion(3.138888888888889e-06, 7.65e-06), 1.850000023841858f),
    Hadar(EquatorialCoordinate(-60.373035, 210.95585541666665), 0.5799999833106995f, ProperMotion(-6.433333333333333e-06, -9.241666666666668e-06), -0.23f),
    Altair(EquatorialCoordinate(8.868321111111111, 297.69582708333326), 0.7599999904632568f, ProperMotion(0.000107025, 0.00014895277777777778), 0.2200000286102295f),
    Acrux(EquatorialCoordinate(-63.09909277777778, 186.6495633333333), 0.76f, ProperMotion(-4.127777777777778e-06, -9.952777777777778e-06), -0.26f),
    Aldebaran(EquatorialCoordinate(16.50930222222222, 68.98016249999999), 0.8600000143051147f, ProperMotion(-5.248333333333333e-05, 1.7625e-05), 1.540000081062317f),
    Antares(EquatorialCoordinate(-26.432002500000003, 247.35191541666666), 0.9100000262260437f, ProperMotion(-6.4722222222222225e-06, -3.3638888888888887e-06), 1.8399999141693115f),
    Spica(EquatorialCoordinate(-11.161319444444445, 201.2982470833333), 0.9700000286102295f, ProperMotion(-8.519444444444444e-06, -1.1763888888888889e-05), -0.23000001907348633f),
    Pollux(EquatorialCoordinate(28.02619888888889, 116.32895749999999), 1.1399999856948853f, ProperMotion(-1.2722222222222221e-05, -0.00017404166666666666), 1.0000001192092896f),
    Fomalhaut(EquatorialCoordinate(-29.622236944444445, 344.41269249999993), 1.159999966621399f, ProperMotion(-4.574166666666666e-05, 9.1375e-05), 0.09000003337860107f),
    Deneb(EquatorialCoordinate(45.28033861111111, 310.3579795833333), 1.25f, ProperMotion(5.138888888888889e-07, 5.583333333333333e-07), 0.09000003337860107f),
    Mimosa(EquatorialCoordinate(-59.68877194444444, 191.93028624999997), 1.25f, ProperMotion(-4.4944444444444445e-06, -1.1936111111111111e-05), -0.23000001907348633f),
    Regulus(EquatorialCoordinate(11.967208611111111, 152.0929620833333), 1.399999976158142f, ProperMotion(1.5527777777777778e-06, -6.909166666666666e-05), -0.15999996662139893f),
    Adhara(EquatorialCoordinate(-28.97208611111111, 104.65645291666665), 1.5f, ProperMotion(3.6944444444444447e-07, 9.000000000000001e-07), -0.21000003814697266f),
    Castor(EquatorialCoordinate(31.888282222222223, 113.64947124999998), 1.5800000429153442f, ProperMotion(-4.0330555555555554e-05, -5.3180555555555555e-05), 0.039999961853027344f),
    Shaula(EquatorialCoordinate(-37.10382333333334, 263.4021670833333), 1.6299999952316284f, ProperMotion(-8.555555555555556e-06, -2.369444444444444e-06), -0.13999998569488525f),
    Gacrux(EquatorialCoordinate(-57.113213333333334, 187.79149833333332), 1.6399999856948853f, ProperMotion(-7.363333333333333e-05, 7.841666666666666e-06), 1.590000033378601f),
    Bellatrix(EquatorialCoordinate(6.349703055555556, 81.28276333333334), 1.6399999856948853f, ProperMotion(-3.577777777777778e-06, -2.2527777777777774e-06), -0.2200000286102295f),
    Elnath(EquatorialCoordinate(28.60745166666667, 81.57297125), 1.649999976158142f, ProperMotion(-4.821666666666667e-05, 6.322222222222223e-06), -0.12999999523162842f),
    Miaplacidus(EquatorialCoordinate(-69.7172075, 138.29990583333333), 1.690000057220459f, ProperMotion(3.026388888888889e-05, -4.346388888888889e-05), 0.0f),
    Alnilam(EquatorialCoordinate(-1.2019188888888888, 84.05338875), 1.690000057220459f, ProperMotion(-2.1666666666666667e-07, 4e-07), -0.18000006675720215f),
    Alnair(EquatorialCoordinate(-46.96097416666667, 332.05826958333324), 1.7100000381469727f, ProperMotion(-4.096388888888889e-05, 3.5191666666666664e-05), -0.12999999523162842f),
    Alnitak(EquatorialCoordinate(-1.9425733333333333, 85.18969416666665), 1.7699999809265137f, ProperMotion(5.638888888888888e-07, 8.861111111111111e-07), -0.21000003814697266f),
    Alioth(EquatorialCoordinate(55.95982277777778, 193.50728958333332), 1.7699999809265137f, ProperMotion(-2.288888888888889e-06, 3.108611111111111e-05), -0.019999980926513672f),
    Dubhe(EquatorialCoordinate(61.75103444444444, 165.93196458333333), 1.7899999618530273f, ProperMotion(-9.63888888888889e-06, -3.725277777777778e-05), 1.0699999332427979f),
    Mirfak(EquatorialCoordinate(49.861179166666666, 51.08070833333333), 1.7899999618530273f, ProperMotion(-7.286111111111111e-06, 6.597222222222222e-06), 0.48000001907348633f),
    Wezen(EquatorialCoordinate(-26.393199444444445, 107.09785), 1.840000033378601f, ProperMotion(9.194444444444444e-07, -8.666666666666667e-07), 0.6799999475479126f),
    Regor(EquatorialCoordinate(-47.33658611111111, 122.38312541666666), 1.8300000429153442f, ProperMotion(2.897222222222222e-06, -1.6861111111111112e-06), -0.25f),
    Sargas(EquatorialCoordinate(-42.99782777777778, 264.3297075), 1.850000023841858f, ProperMotion(-8.666666666666667e-07, 1.538888888888889e-06), 0.43999993801116943f),
    KausAustralis(EquatorialCoordinate(-34.38461638888889, 276.0429933333333), 1.809999942779541f, ProperMotion(-3.45e-05, -1.095e-05), 0.010000109672546387f),
    Avior(EquatorialCoordinate(-59.50948416666667, 125.62848), 1.8600000143051147f, ProperMotion(6.127777777777777e-06, -7.088888888888889e-06), 1.2700001001358032f),
    Alkaid(EquatorialCoordinate(49.313266666666664, 206.8851570833333), 1.8600000143051147f, ProperMotion(-4.141666666666667e-06, -3.3658333333333336e-05), -0.19000005722045898f),
    Menkalinan(EquatorialCoordinate(44.9474325, 89.88217875), 1.899999976158142f, ProperMotion(-2.638888888888889e-07, -1.5677777777777776e-05), 0.029999971389770508f),
    Atria(EquatorialCoordinate(-69.02771166666666, 252.16622916666665), 1.8799999952316284f, ProperMotion(-8.772222222222222e-06, 4.997222222222222e-06), 1.4499999284744263f),
    Alhena(EquatorialCoordinate(16.399280277777777, 99.42796041666666), 1.9199999570846558f, ProperMotion(-1.5266666666666667e-05, 3.836111111111111e-06), 0.0f),
    Peacock(EquatorialCoordinate(-56.73508972222222, 306.41190416666666), 1.9179999828338623f, ProperMotion(-2.3894444444444442e-05, 1.916666666666667e-06), -0.12699997425079346f),
    Alsephina(EquatorialCoordinate(-54.708820833333334, 131.17594291666663), 1.95f, ProperMotion(-2.8916666666666664e-05, 8e-06), 0.04f),
    Mirzam(EquatorialCoordinate(-17.95591861111111, 95.67493874999998), 1.9700000286102295f, ProperMotion(-2.1666666666666667e-07, -8.972222222222223e-07), -0.24000000953674316f),
    Polaris(EquatorialCoordinate(89.26410888888888, 37.95456041666666), 2.0199999809265137f, ProperMotion(-3.2916666666666668e-06, 1.2355555555555555e-05), 0.5999999046325684f),
    Alphard(EquatorialCoordinate(-8.658599444444445, 141.8968445833333), 1.9700000286102295f, ProperMotion(9.547222222222222e-06, -4.230555555555556e-06), 1.4500000476837158f),
    Hamal(EquatorialCoordinate(23.4624175, 31.793357083333326), 2.009999990463257f, ProperMotion(-4.1133333333333335e-05, 5.2375000000000006e-05), 1.1600000858306885f),
    Diphda(EquatorialCoordinate(-17.98660611111111, 10.89737875), 2.009999990463257f, ProperMotion(8.88611111111111e-06, 6.459722222222222e-05), 1.0099999904632568f),
    Alpheratz(EquatorialCoordinate(29.09043111111111, 2.096915833333333), 2.059999942779541f, ProperMotion(-4.54e-05, 3.818333333333334e-05), -0.1099998950958252f),
    Ankaa(EquatorialCoordinate(-42.30598694444444, 6.571047499999999), 2.380000114440918f, ProperMotion(-9.897222222222223e-05, 6.473611111111111e-05), 1.0899999141693115f),
    Schedar(EquatorialCoordinate(56.537329166666666, 10.126845833333332), 2.2300000190734863f, ProperMotion(-8.776388888888889e-06, 1.364611111111111e-05), 1.1700000762939453f),
    Acamar(EquatorialCoordinate(-40.30468111111111, 44.56531333333333), 3.2f, ProperMotion(6.105555555555556e-06, -1.4691666666666667e-05), 0.128f),
    Menkar(EquatorialCoordinate(4.089738611111111, 45.56988749999999), 2.5299999713897705f, ProperMotion(-2.134722222222222e-05, -2.891666666666667e-06), 1.6400001049041748f),
    Capella(EquatorialCoordinate(45.99799138888889, 79.17232791666666), 0.07999999821186066f, ProperMotion(-0.00011858055555555555, 2.0902777777777778e-05), 0.800000011920929f),
    Suhail(EquatorialCoordinate(-43.43259083333333, 136.99899083333332), 2.2100000381469727f, ProperMotion(3.7555555555555553e-06, -6.669444444444445e-06), 1.6499998569488525f),
    Denebola(EquatorialCoordinate(14.572058055555555, 177.26490958333332), 2.130000114440918f, ProperMotion(-3.1852777777777777e-05, -0.00013824444444444445), 0.08999991416931152f),
    Gienah(EquatorialCoordinate(-17.54193027777778, 183.95154499999998), 2.5799999237060547f, ProperMotion(6.072222222222222e-06, -4.405833333333334e-05), -0.1099998950958252f),
    Menkent(EquatorialCoordinate(-36.369954722222225, 211.6706145833333), 2.049999952316284f, ProperMotion(-0.00014390555555555555, -0.00014459166666666665), 0.9900000095367432f),
    Zubenelgenubi(EquatorialCoordinate(-16.04177638888889, 222.7196375), 2.75f, ProperMotion(-1.9e-05, -2.935555555555556e-05), 0.15000009536743164f),
    Kochab(EquatorialCoordinate(74.1555038888889, 222.6763575), 2.0799999237060547f, ProperMotion(3.1722222222222223e-06, -9.058333333333333e-06), 1.4700000286102295f),
    Alphecca(EquatorialCoordinate(26.714685, 233.67195166666664), 2.240000009536743f, ProperMotion(-2.4364166666666666e-05, 3.303527777777778e-05), -0.019999980926513672f),
    Sabik(EquatorialCoordinate(-15.72490638888889, 257.5945283333333), 2.4200000762939453f, ProperMotion(2.7547222222222224e-05, 1.1147222222222223e-05), 0.04999995231628418f),
    Rasalhague(EquatorialCoordinate(12.560037222222222, 263.73362249999997), 2.069999933242798f, ProperMotion(-6.154722222222222e-05, 3.001944444444444e-05), 0.15000009536743164f),
    Eltanin(EquatorialCoordinate(51.48889555555556, 269.1515408333333), 2.2300000190734863f, ProperMotion(-6.330555555555555e-06, -2.3555555555555555e-06), 1.5299999713897705f),
    Nunki(EquatorialCoordinate(-26.29672388888889, 283.8163604166666), 2.066999912261963f, ProperMotion(-1.4841666666666667e-05, 4.205555555555556e-06), -0.14399993419647217f),
    Enif(EquatorialCoordinate(9.875008611111111, 326.04648374999994), 2.390000104904175f, ProperMotion(1.2222222222222222e-07, 7.477777777777779e-06), 1.5199999809265137f),
    Markab(EquatorialCoordinate(15.205266944444444, 346.19022249999995), 2.4800000190734863f, ProperMotion(-1.1472222222222221e-05, 1.6777777777777776e-05), -0.039999961853027344f),
    Merak(EquatorialCoordinate(56.38243361111111, 165.46033208333333), 2.369999885559082f, ProperMotion(8.990277777777778e-06, 2.2210833333333334e-05), -0.019999980926513672f),
    Phecda(EquatorialCoordinate(53.694759722222216, 178.4576970833333), 2.440000057220459f, ProperMotion(3.0583333333333334e-06, 2.9911111111111115e-05), 0.009999990463256836f),
    Megrez(EquatorialCoordinate(57.03261694444444, 183.85649916666665), 3.319999933242798f, ProperMotion(2.261388888888889e-06, 2.887416666666667e-05), 0.09000015258789062f),
    Mizar(EquatorialCoordinate(54.925351944444444, 200.9814183333333), 2.04f, ProperMotion(-7.213888888888888e-06, 3.3058333333333335e-05), 0.02f),
    Imai(EquatorialCoordinate(-58.74892388888889, 183.78632666666664), 2.752000093460083f, ProperMotion(-3.1377777777777774e-06, -1.0264166666666667e-05), -0.18400001525878906f),
    Ginan(EquatorialCoordinate(-60.40114805555555, 185.34003374999998), 3.569999933242798f, ProperMotion(2.5498611111111112e-05, -4.7561944444444445e-05), 1.4100000858306885f),
    Saiph(EquatorialCoordinate(-9.669604722222221, 86.93911999999999), 2.059999942779541f, ProperMotion(-3.555555555555556e-07, 4.055555555555555e-07), -0.1799999475479126f),
    Meissa(EquatorialCoordinate(9.934155833333334, 83.78448999999998), 3.6600000858306885f, ProperMotion(-8.166666666666666e-07, -9.444444444444445e-08), -0.18000006675720215f),
    Mintaka(EquatorialCoordinate(-0.299095, 83.00166666666665), 2.4100000858306885f, ProperMotion(-1.9166666666666665e-07, 1.777777777777778e-07), -0.3900001049041748f),
    Mirach(EquatorialCoordinate(35.620557500000004, 17.433015833333332), 2.049999952316284f, ProperMotion(-3.116666666666667e-05, 4.8861111111111114e-05), 1.5699999332427979f),
    Algieba(EquatorialCoordinate(19.841485, 154.99312708333332), 2.369999885559082f, ProperMotion(-4.2855555555555554e-05, 8.452777777777778e-05), 1.4200000762939453f),
    Algol(EquatorialCoordinate(40.95564666666667, 47.04221833333333), 2.119999885559082f, ProperMotion(-4.6111111111111107e-07, 8.305555555555556e-07), -0.04999995231628418f),
    Tiaki(EquatorialCoordinate(-46.88457638888889, 340.6668758333333), 2.109999895095825f, ProperMotion(-1.2166666666666667e-06, 3.754444444444444e-05), 1.6200001239776611f),
    GammaCentauri(EquatorialCoordinate(-48.95987138888889, 190.3793333333333), 2.1700000762939453f, ProperMotion(1.6083333333333333e-06, -5.158888888888889e-05), -0.009999990463256836f),
    Aspidiske(EquatorialCoordinate(-59.27523194444444, 139.27252833333333), 2.259999990463257f, ProperMotion(3.3277777777777777e-06, -5.2388888888888885e-06), 0.18000006675720215f),
    Sadr(EquatorialCoordinate(40.25667888888889, 305.5570908333333), 2.2300000190734863f, ProperMotion(-2.527777777777778e-07, 6.638888888888889e-07), 0.6700000762939453f),
    Naos(EquatorialCoordinate(-40.00314777777778, 120.89603125), 2.25f, ProperMotion(4.633333333333334e-06, -8.252777777777779e-06), -0.26999998092651367f),
    Almach(EquatorialCoordinate(42.329728333333335, 30.97480083333333), 2.0999999046325684f, ProperMotion(-1.3694444444444443e-05, 1.1755555555555556e-05), 1.2000000476837158f),
    Caph(EquatorialCoordinate(59.14978111111111, 2.29452125), 2.2699999809265137f, ProperMotion(-4.9936111111111113e-05, 0.00014541666666666666), 0.3399999141693115f),
    AlphaLupi(EquatorialCoordinate(-47.38819861111111, 220.48231541666667), 2.2860000133514404f, ProperMotion(-6.5750000000000006e-06, -5.816666666666667e-06), -0.16000008583068848f),
    EpsilonCentauri(EquatorialCoordinate(-53.466391111111115, 204.97190708333332), 2.299999952316284f, ProperMotion(-3.255555555555556e-06, -4.25e-06), -0.2200000286102295f),
    Dschubba(EquatorialCoordinate(-22.62170638888889, 240.08335499999995), 2.319999933242798f, ProperMotion(-9.83611111111111e-06, -2.8361111111111113e-06), -0.11999988555908203f),
    Larawag(EquatorialCoordinate(-34.293231388888884, 252.54087833333327), 2.2899999618530273f, ProperMotion(-7.110555555555556e-05, -0.00017079166666666667), 1.1600000858306885f),
    EtaCentauri(EquatorialCoordinate(-42.157824999999995, 218.8767670833333), 2.309999942779541f, ProperMotion(-9.088888888888888e-06, -9.647222222222221e-06), -0.19000005722045898f),
    KappaScorpii(EquatorialCoordinate(-39.029983055555554, 265.62197999999995), 2.385999917984009f, ProperMotion(-7.094444444444444e-06, -1.6805555555555554e-06), -0.17499995231628418f),
    Scheat(EquatorialCoordinate(28.082786944444443, 345.94357249999996), 2.4200000762939453f, ProperMotion(3.8036111111111116e-05, 5.2125e-05), 1.6700000762939453f),
    Aludra(EquatorialCoordinate(-29.303105277777778, 111.02375916666666), 2.450000047683716f, ProperMotion(1.6138888888888887e-06, -1.15e-06), -0.08000016212463379f),
    Alderamin(EquatorialCoordinate(62.58557444444445, 319.6448845833333), 2.4600000381469727f, ProperMotion(1.3636111111111112e-05, 4.181944444444445e-05), 0.2200000286102295f),
    Markeb(EquatorialCoordinate(-26.80383861111111, 114.70779458333331), 2.48f, ProperMotion(5.9111111111111115e-06, -4.622222222222222e-06), -0.2f),
    GammaCassiopeiae(EquatorialCoordinate(60.71674, 14.1772125), 2.390000104904175f, ProperMotion(-1.0888888888888889e-06, 6.991666666666667e-06), -0.10000014305114746f),
    Aljanah(EquatorialCoordinate(33.970328333333335, 311.55280083333327), 2.4800000190734863f, ProperMotion(8.577416666666666e-05, 0.0001016538888888889), 1.0399999618530273f),
    Acrab(EquatorialCoordinate(-19.80538888888889, 241.3592916666666), 2.5f, ProperMotion(-6.677777777777778e-06, -1.4444444444444445e-06), -0.06999993324279785f),
}

/**
 * Proper motion of a star in degrees per year
 */
data class ProperMotion(val declination: Double, val rightAscension: Double)