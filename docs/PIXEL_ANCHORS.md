# Measured anchors (from the dark references; re-measure with tools/measure.py before relying on any other number)

Unit: px at 1080x2340, dp = px / 2.625. Fractional dp is expected; use the RefPx helper (px / 2.625f).

- Page background #101112. Bars and nav surfaces #141414. Dividers #3B3C3D / #404040 (3 px).
- Text: primary #FFFFFF, secondary #A8ABAD, tertiary #8E9194. Accent blue #0274E6 (highlight row), #2491FF (checkmarks and active icons).
- Contents panel background #232323. Display-settings sheet #2C2C2E. Annotation sheets #292A2B.
- Status bar content y 24-58. Library title band y 145-196 (52 px). Search bar y 254-358 (105 px, about 40 dp), divider y 401-403.
- Library grid first row starts y 472; row pitch 474 px (180.6 dp); cover block 427 px tall.
- Library list: row pitch 318 px (121 dp) with a 3 px divider.
- Gesture bar region y 2324-2339.

Measure everything else the same way and extend this file.
