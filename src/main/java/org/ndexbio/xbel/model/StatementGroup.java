//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.12.03 at 11:33:45 AM PST 
//


package org.ndexbio.xbel.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{http://belframework.org/schema/1.0/xbel}name" minOccurs="0"/>
 *         &lt;element ref="{http://belframework.org/schema/1.0/xbel}comment" minOccurs="0"/>
 *         &lt;element ref="{http://belframework.org/schema/1.0/xbel}annotationGroup" minOccurs="0"/>
 *         &lt;element ref="{http://belframework.org/schema/1.0/xbel}statement" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://belframework.org/schema/1.0/xbel}statementGroup" maxOccurs="unbounded" minOccurs="0"/>
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
    "name",
    "comment",
    "annotationGroup",
    "statement",
    "statementGroup"
})
@XmlRootElement(name = "statementGroup")
public class StatementGroup {

    protected String name;
    protected String comment;
    protected AnnotationGroup annotationGroup;
    protected List<Statement> statement;
    protected List<StatementGroup> statementGroup;

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
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the annotationGroup property.
     * 
     * @return
     *     possible object is
     *     {@link AnnotationGroup }
     *     
     */
    public AnnotationGroup getAnnotationGroup() {
        return annotationGroup;
    }

    /**
     * Sets the value of the annotationGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link AnnotationGroup }
     *     
     */
    public void setAnnotationGroup(AnnotationGroup value) {
        this.annotationGroup = value;
    }

    /**
     * Gets the value of the statement property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the statement property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStatement().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Statement }
     * 
     * 
     */
    public List<Statement> getStatement() {
        if (statement == null) {
            statement = new ArrayList<Statement>();
        }
        return this.statement;
    }

    /**
     * Gets the value of the statementGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the statementGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStatementGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StatementGroup }
     * 
     * 
     */
    public List<StatementGroup> getStatementGroup() {
        if (statementGroup == null) {
            statementGroup = new ArrayList<StatementGroup>();
        }
        return this.statementGroup;
    }

}
