//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.01.23 at 04:26:22 PM IST 
//


package com.izn.schemamodeler.ui3.portal;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

  

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "schema"
})
@XmlRootElement(name = "component")
public class PortalXMLSchema {

    protected List<PortalXMLSchema.Schema> schema;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "version")
    protected String version;

    /**
     * Gets the value of the schema property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schema property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchema().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PortalXMLSchema.Schema }
     * 
     * 
     */
    public List<PortalXMLSchema.Schema> getSchema() {
        if (schema == null) {
            schema = new ArrayList<PortalXMLSchema.Schema>();
        }
        return this.schema;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "basic",
        "field"
    })
    public static class Schema {

        @XmlElement(required = true)
        protected PortalXMLSchema.Schema.Basic basic;
        @XmlElement(required = true)
        protected PortalXMLSchema.Schema.Field field;

        /**
         * Gets the value of the basic property.
         * 
         * @return
         *     possible object is
         *     {@link PortalXMLSchema.Schema.Basic }
         *     
         */
        public PortalXMLSchema.Schema.Basic getBasic() {
            return basic;
        }

        /**
         * Sets the value of the basic property.
         * 
         * @param value
         *     allowed object is
         *     {@link PortalXMLSchema.Schema.Basic }
         *     
         */
        public void setBasic(PortalXMLSchema.Schema.Basic value) {
            this.basic = value;
        }

        /**
         * Gets the value of the field property.
         * 
         * @return
         *     possible object is
         *     {@link PortalXMLSchema.Schema.Field }
         *     
         */
        public PortalXMLSchema.Schema.Field getField() {
            return field;
        }

        /**
         * Sets the value of the field property.
         * 
         * @param value
         *     allowed object is
         *     {@link PortalXMLSchema.Schema.Field }
         *     
         */
        public void setField(PortalXMLSchema.Schema.Field value) {
            this.field = value;
        }


       
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class Basic {

            @XmlValue
            protected String value;
            @XmlAttribute(name = "name")
            protected String name;
            @XmlAttribute(name = "description")
            protected String description;
            @XmlAttribute(name = "hidden")
            protected String hidden;
            @XmlAttribute(name = "registryName")
            protected String registryName;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
            }

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the description property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDescription() {
                return description;
            }

            /**
             * Sets the value of the description property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDescription(String value) {
                this.description = value;
            }

            /**
             * Gets the value of the hidden property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHidden() {
                return hidden;
            }

            /**
             * Sets the value of the hidden property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHidden(String value) {
                this.hidden = value;
            }

            /**
             * Gets the value of the registryName property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getRegistryName() {
                return registryName;
            }

            /**
             * Sets the value of the registryName property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setRegistryName(String value) {
                this.registryName = value;
            }

        }


  
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "detail",
            "setting",
            "items"
        })
        public static class Field {

            protected List<PortalXMLSchema.Schema.Field.Detail> detail;
            @XmlElement(required = true)
            protected PortalXMLSchema.Schema.Field.Setting setting;
            @XmlElement(required = true)
            protected PortalXMLSchema.Schema.Field.Items items;
            @XmlAttribute(name = "type")
            protected String type;

            /**
             * Gets the value of the detail property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the detail property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getDetail().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link PortalXMLSchema.Schema.Field.Detail }
             * 
             * 
             */
            public List<PortalXMLSchema.Schema.Field.Detail> getDetail() {
                if (detail == null) {
                    detail = new ArrayList<PortalXMLSchema.Schema.Field.Detail>();
                }
                return this.detail;
            }

            /**
             * Gets the value of the setting property.
             * 
             * @return
             *     possible object is
             *     {@link PortalXMLSchema.Schema.Field.Setting }
             *     
             */
            public PortalXMLSchema.Schema.Field.Setting getSetting() {
                return setting;
            }

            /**
             * Sets the value of the setting property.
             * 
             * @param value
             *     allowed object is
             *     {@link PortalXMLSchema.Schema.Field.Setting }
             *     
             */
            public void setSetting(PortalXMLSchema.Schema.Field.Setting value) {
                this.setting = value;
            }

            /**
             * Gets the value of the items property.
             * 
             * @return
             *     possible object is
             *     {@link PortalXMLSchema.Schema.Field.Items }
             *     
             */
            public PortalXMLSchema.Schema.Field.Items getItems() {
                return items;
            }

            /**
             * Sets the value of the items property.
             * 
             * @param value
             *     allowed object is
             *     {@link PortalXMLSchema.Schema.Field.Items }
             *     
             */
            public void setItems(PortalXMLSchema.Schema.Field.Items value) {
                this.items = value;
            }

            /**
             * Gets the value of the type property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getType() {
                return type;
            }

            /**
             * Sets the value of the type property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setType(String value) {
                this.type = value;
            }


            /**
             * <p>Java class for anonymous complex type.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;simpleContent>
             *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
             *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
             *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
             *     &lt;/extension>
             *   &lt;/simpleContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value"
            })
            public static class Detail {

                @XmlValue
                protected String value;
                @XmlAttribute(name = "name")
                protected String name;
                @XmlAttribute(name = "value")
                protected String valueAttribute;

                /**
                 * Gets the value of the value property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getValue() {
                    return value;
                }

                /**
                 * Sets the value of the value property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setValue(String value) {
                    this.value = value;
                }

                /**
                 * Gets the value of the name property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getName() {
                    return name;
                }

                /**
                 * Sets the value of the name property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setName(String value) {
                    this.name = value;
                }

                /**
                 * Gets the value of the valueAttribute property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getValueAttribute() {
                    return valueAttribute;
                }

                /**
                 * Sets the value of the valueAttribute property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setValueAttribute(String value) {
                    this.valueAttribute = value;
                }

            }


            /**
             * <p>Java class for anonymous complex type.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="item" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "item"
            })
            public static class Items {
            	protected List<PortalXMLSchema.Schema.Field.Items.Item> item;
            	 public List<PortalXMLSchema.Schema.Field.Items.Item> getItem() {
                     if (item == null) {
                         item = new ArrayList<PortalXMLSchema.Schema.Field.Items.Item>();
                     }
                     return this.item;
                 }

                /**
                 * Gets the value of the item property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the item property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getItem().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link String }
                 * 
                 * 
                 */
            
            	 @XmlAccessorType(XmlAccessType.FIELD)
                 @XmlType(name = "", propOrder = {
                     "value"
                 })
                 public static class Item {

                     @XmlValue
                     protected String value;
                     @XmlAttribute(name = "type")
                     protected String type;

                     /**
                      * Gets the value of the value property.
                      * 
                      * @return
                      *     possible object is
                      *     {@link String }
                      *     
                      */
                     public String getValue() {
                         return value;
                     }

                     /**
                      * Sets the value of the value property.
                      * 
                      * @param value
                      *     allowed object is
                      *     {@link String }
                      *     
                      */
                     public void setValue(String value) {
                         this.value = value;
                     }

                     /**
                      * Gets the value of the type property.
                      * 
                      * @return
                      *     possible object is
                      *     {@link String }
                      *     
                      */
                     public String getType() {
                         return type;
                     }

                     /**
                      * Sets the value of the type property.
                      * 
                      * @param value
                      *     allowed object is
                      *     {@link String }
                      *     
                      */
                     public void setType(String value) {
                         this.type = value;
                     }

                 }

                
                
            }


            /**
             * <p>Java class for anonymous complex type.
             * 
             * <p>The following schema fragment specifies the expected content contained within this class.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="param" maxOccurs="unbounded" minOccurs="0">
             *           &lt;complexType>
             *             &lt;simpleContent>
             *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
             *                 &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
             *                 &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
             *               &lt;/extension>
             *             &lt;/simpleContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "param"
            })
            public static class Setting {

                protected List<PortalXMLSchema.Schema.Field.Setting.Param> param;

                /**
                 * Gets the value of the param property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the param property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getParam().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link PortalXMLSchema.Schema.Field.Setting.Param }
                 * 
                 * 
                 */
                public List<PortalXMLSchema.Schema.Field.Setting.Param> getParam() {
                    if (param == null) {
                        param = new ArrayList<PortalXMLSchema.Schema.Field.Setting.Param>();
                    }
                    return this.param;
                }


                /**
                 * <p>Java class for anonymous complex type.
                 * 
                 * <p>The following schema fragment specifies the expected content contained within this class.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;simpleContent>
                 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
                 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
                 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
                 *     &lt;/extension>
                 *   &lt;/simpleContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                    "value"
                })
                public static class Param {

                    @XmlValue
                    protected String value;
                    @XmlAttribute(name = "name")
                    protected String name;
                    @XmlAttribute(name = "value")
                    protected String valueAttribute;

                    /**
                     * Gets the value of the value property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getValue() {
                        return value;
                    }

                    /**
                     * Sets the value of the value property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setValue(String value) {
                        this.value = value;
                    }

                    /**
                     * Gets the value of the name property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getName() {
                        return name;
                    }

                    /**
                     * Sets the value of the name property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setName(String value) {
                        this.name = value;
                    }

                    /**
                     * Gets the value of the valueAttribute property.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getValueAttribute() {
                        return valueAttribute;
                    }

                    /**
                     * Sets the value of the valueAttribute property.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setValueAttribute(String value) {
                        this.valueAttribute = value;
                    }

                }

            }

        }

    }

}