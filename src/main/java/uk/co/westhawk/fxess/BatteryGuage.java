
package uk.co.westhawk.fxess;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.KnobType;
import eu.hansolo.medusa.Gauge.LedType;
import eu.hansolo.medusa.Gauge.NeedleShape;
import eu.hansolo.medusa.Gauge.NeedleSize;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.LcdDesign;
import eu.hansolo.medusa.LcdFont;
import eu.hansolo.medusa.TickLabelLocation;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.TickMarkType;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
/**
 *
 * @author thp
 */
public class BatteryGuage {
    public static Gauge make(){
    Gauge gauge;
        gauge = GaugeBuilder.create()
                .skinType(SkinType.BATTERY) 
                .barBackgroundColor(Color.SEAGREEN)
                .barColor(Color.GREENYELLOW)// Skin for your Gauge
                .prefSize(240   ,240)                                                               // Preferred size of control
                // Related to Foreground Elements
                .foregroundBaseColor(Color.GREENYELLOW)                                                // Color for title, subtitle, unit, value, tick label, zeroColor, tick mark, major tick mark, medium tick mark and minor tick mark
                // Related to Title Text
                .title("Title")                                                                  // Text for title
                .titleColor(Color.GOLDENROD)                                                         // Color for title text
                // Related to Sub Title Text
                .subTitle("SubTitle")                                                            // Text for subtitle
                .subTitleColor(Color.AZURE)                                                      // Color for subtitle text
                // Related to Unit Text
                .unit("Unit")                                                                    // Text for unit
                .unitColor(Color.PEACHPUFF)                                                          // Color for unit text
                // Related to Value Text
                .valueColor(Color.BLUEVIOLET)                                                         // Color for value text
                .decimals(0)                                                                     // Number of decimals for the value/lcd text
                // Related to LCD
                .lcdVisible(false)                                                               // LCD instead of the plain value text
                .lcdDesign(LcdDesign.STANDARD)                                                   // Design for LCD
                .lcdFont(LcdFont.DIGITAL_BOLD)                                                   // Font for LCD (STANDARD, LCD, DIGITAL, DIGITAL_BOLD, ELEKTRA)
                // Related to scale
                                       // Direction of Scale (CLOCKWISE, COUNTER_CLOCKWISE)
                .minValue(0)                                                                     // Start value of Scale
                .maxValue(100)                                                                   // End value of Scale
                .startAngle(320)                                                                 // Start angle of Scale (bottom -> 0, direction -> CCW)
                .angleRange(280)                                                                 // Angle range of Scale starting from the start angle
                // Related to Tick Labels
                .tickLabelDecimals(0)                                                            // Number of decimals for tick labels
                .tickLabelLocation(TickLabelLocation.INSIDE)                                     // Should tick labels be inside or outside Scale (INSIDE, OUTSIDE)
                .tickLabelOrientation(TickLabelOrientation.HORIZONTAL)                           // Orientation of tick labels (ORTHOGONAL,  HORIZONTAL, TANGENT)
                .onlyFirstAndLastTickLabelVisible(false)                                         // Should only the first and last tick label be visible
                .tickLabelSectionsVisible(false)                                                 // Should sections for tick labels be visible
                .tickLabelColor(Color.BISQUE)                                                     // Color for tick labels (overriden by tick label sections)
                // Related to Tick Marks
                .tickMarkSectionsVisible(false)                                                  // Should sections for tick marks be visible
                // Related to Major Tick Marks
                .majorTickMarksVisible(true)                                                     // Should major tick marks be visible
                .majorTickMarkType(TickMarkType.LINE)                                            // Tick mark type for major tick marks (LINE, DOT, TRIANGLE, TICK_LABEL)
                .majorTickMarkColor(Color.LAVENDER)                                                 // Color for major tick marks (overriden by tick mark sections)
                // Related to Medium Tick Marks
                .mediumTickMarksVisible(true)                                                    // Should medium tick marks be visible
                .mediumTickMarkType(TickMarkType.LINE)                                           // Tick mark type for medium tick marks (LINE, DOT, TRIANGLE)
                .mediumTickMarkColor(Color.OLIVE)                                                // Color for medium tick marks (overriden by tick mark sections)
                // Related to Minor Tick Marks
                .minorTickMarksVisible(true)                                                     // Should minor tick marks be visible
                .minorTickMarkType(TickMarkType.LINE)                                            // Tick mark type for minor tick marks (LINE, DOT, TRIANGLE)
                .minorTickMarkColor(Color.OLDLACE)                                                 // Color for minor tick marks (override by tick mark sections)
                // Related to LED
                .ledVisible(false)                                                               // Should LED be visible
                .ledType(LedType.STANDARD)                                                       // Type of the LED (STANDARD, FLAT)
                .ledColor(Color.rgb(255, 200, 0))                                                // Color of LED
                .ledBlinking(false)                                                              // Should LED blink
                .ledOn(false)                                                                    // LED on or off
                // Related to Needle
                .needleShape(NeedleShape.ANGLED)                                                 // Shape of needle (ANGLED, ROUND, FLAT)
                .needleSize(NeedleSize.STANDARD)                                                 // Size of needle (THIN, STANDARD, THICK)
                .needleColor(Color.CRIMSON)                                                      // Color of needle
                // Related to Needle behavior
                .startFromZero(false)                                                            // Should needle start from the 0 value
                .returnToZero(false)                                                             // Should needle return to the 0 value (only makes sense when animated==true)
                // Related to Knob
                .knobType(KnobType.STANDARD)                                                     // Type for center knob (STANDARD, PLAIN, METAL, FLAT)
                .knobColor(Color.CHOCOLATE)                                                      // Color of center knob
                .interactive(false)                                                              // Should center knob be act as button
                .onButtonPressed(buttonEvent -> System.out.println("Knob pressed"))              // Handler (triggered when the center knob was pressed)
                .onButtonReleased(buttonEvent -> System.out.println("Knob released"))            // Handler (triggered when the center knob was released)
                // Related to Threshold
                .thresholdVisible(false)                                                         // Should threshold indicator be visible
                .threshold(50)                                                                   // Value of threshold
                .thresholdColor(Color.RED)                                                       // Color of threshold indicator
                .checkThreshold(false)                                                           // Should each value be checked against threshold
                .onThresholdExceeded(thresholdEvent -> System.out.println("Threshold exceeded")) // Handler (triggered if checkThreshold==true and the threshold is exceeded)
                .onThresholdUnderrun(thresholdEvent -> System.out.println("Threshold underrun")) // Handler (triggered if checkThreshold==true and the threshold is underrun)
                // Related to Gradient Bar
                .gradientBarEnabled(false)                                                       // Should gradient filled bar be visible to visualize a range
                .gradientBarStops(new Stop(0.0, Color.BLUE),                                     // Color gradient that will be use to color fill bar
                        new Stop(0.25, Color.CYAN),
                        new Stop(0.5, Color.LIME),
                        new Stop(0.75, Color.YELLOW),
                        new Stop(1.0, Color.RED))
                // Related to Value
                .animated(false)                                                                 // Should needle be animated
                .animationDuration(500)                                                          // Speed of needle in milliseconds (10 - 10000 ms)
                .build();
    return gauge;
    }
}
