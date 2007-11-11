<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:doc="http://nwalsh.com/xsl/documentation/1.0" xmlns:src="http://nwalsh.com/xmlns/litprog/fragment" exclude-result-prefixes="src" version="1.0">


<doc:refentry><refnamediv>
<refname>dot.count</refname>
<refpurpose>Returns the number of <quote>.</quote> characters in a string</refpurpose>
</refnamediv><refsect1><title>Description</title>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="dot.count">
  <!-- Returns the number of "." characters in a string -->
  <xsl:param name="string"/>
  <xsl:param name="count" select="0"/>
  <xsl:choose>
    <xsl:when test="contains($string, '.')">
      <xsl:call-template name="dot.count">
        <xsl:with-param name="string" select="substring-after($string, '.')"/>
        <xsl:with-param name="count" select="$count+1"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$count"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>copy-string</refname>
<refpurpose>Returns <quote>count</quote> copies of a string</refpurpose>
</refnamediv><refsect1><title>Description</title>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="copy-string">
  <!-- returns 'count' copies of 'string' -->
  <xsl:param name="string"/>
  <xsl:param name="count" select="0"/>
  <xsl:param name="result"/>

  <xsl:choose>
    <xsl:when test="$count&gt;0">
      <xsl:call-template name="copy-string">
        <xsl:with-param name="string" select="$string"/>
        <xsl:with-param name="count" select="$count - 1"/>
        <xsl:with-param name="result">
          <xsl:value-of select="$result"/>
          <xsl:value-of select="$string"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$result"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>string.subst</refname>
<refpurpose>Substitute one text string for another in a string</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>string.subst</function> template replaces all
occurances of <parameter>target</parameter> in <parameter>string</parameter>
with <parameter>replacement</parameter> and returns the result.
</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="string.subst">
  <xsl:param name="string"/>
  <xsl:param name="target"/>
  <xsl:param name="replacement"/>

  <xsl:choose>
    <xsl:when test="contains($string, $target)">
      <xsl:variable name="rest">
        <xsl:call-template name="string.subst">
          <xsl:with-param name="string" select="substring-after($string, $target)"/>
          <xsl:with-param name="target" select="$target"/>
          <xsl:with-param name="replacement" select="$replacement"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:value-of select="concat(substring-before($string, $target),                                    $replacement,                                    $rest)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$string"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>xpointer.idref</refname>
<refpurpose>Extract IDREF from an XPointer</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>xpointer.idref</function> template returns the
ID portion of an XPointer which is a pointer to an ID within the current
document, or the empty string if it is not.</para>
<para>In other words, <function>xpointer.idref</function> returns
<quote>foo</quote> when passed either <literal>#foo</literal>
or <literal>#xpointer(id('foo'))</literal>, otherwise it returns
the empty string.</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="xpointer.idref">
  <xsl:param name="xpointer">http://...</xsl:param>
  <xsl:choose>
    <xsl:when test="starts-with($xpointer, '#xpointer(id(')">
      <xsl:variable name="rest" select="substring-after($xpointer, '#xpointer(id(')"/>
      <xsl:variable name="quote" select="substring($rest, 1, 1)"/>
      <xsl:value-of select="substring-before(substring-after($xpointer, $quote), $quote)"/>
    </xsl:when>
    <xsl:when test="starts-with($xpointer, '#')">
      <xsl:value-of select="substring-after($xpointer, '#')"/>
    </xsl:when>
    <!-- otherwise it's a pointer to some other document -->
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>length-magnitude</refname>
<refpurpose>Return the unqualified dimension from a length specification</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>length-magnitude</function> template returns the
unqualified length ("20" for "20pt") from a dimension.
</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="length-magnitude">
  <xsl:param name="length" select="'0pt'"/>

  <xsl:choose>
    <xsl:when test="string-length($length) = 0"/>
    <xsl:when test="substring($length,1,1) = '0'                     or substring($length,1,1) = '1'                     or substring($length,1,1) = '2'                     or substring($length,1,1) = '3'                     or substring($length,1,1) = '4'                     or substring($length,1,1) = '5'                     or substring($length,1,1) = '6'                     or substring($length,1,1) = '7'                     or substring($length,1,1) = '8'                     or substring($length,1,1) = '9'                     or substring($length,1,1) = '.'">
      <xsl:value-of select="substring($length,1,1)"/>
      <xsl:call-template name="length-magnitude">
        <xsl:with-param name="length" select="substring($length,2)"/>
      </xsl:call-template>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>length-units</refname>
<refpurpose>Return the units from a length specification</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>length-units</function> template returns the
units ("pt" for "20pt") from a length. If no units are supplied on the
length, the <parameter>defauilt.units</parameter> are returned.</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="length-units">
  <xsl:param name="length" select="'0pt'"/>
  <xsl:param name="default.units" select="'px'"/>
  <xsl:variable name="magnitude">
    <xsl:call-template name="length-magnitude">
      <xsl:with-param name="length" select="$length"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="units">
    <xsl:value-of select="substring($length, string-length($magnitude)+1)"/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="$units = ''">
      <xsl:value-of select="$default.units"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$units"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>length-spec</refname>
<refpurpose>Return a fully qualified length specification</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>length-spec</function> template returns the
qualified length from a dimension. If an unqualified length is given,
the <parameter>default.units</parameter> will be added to it.
</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="length-spec">
  <xsl:param name="length" select="'0pt'"/>
  <xsl:param name="default.units" select="'px'"/>

  <xsl:variable name="magnitude">
    <xsl:call-template name="length-magnitude">
      <xsl:with-param name="length" select="$length"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="units">
    <xsl:value-of select="substring($length, string-length($magnitude)+1)"/>
  </xsl:variable>

  <xsl:value-of select="$magnitude"/>
  <xsl:choose>
    <xsl:when test="$units='cm'                     or $units='mm'                     or $units='in'                     or $units='pt'                     or $units='pc'                     or $units='px'                     or $units='em'">
      <xsl:value-of select="$units"/>
    </xsl:when>
    <xsl:when test="$units = ''">
      <xsl:value-of select="$default.units"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message>
        <xsl:text>Unrecognized unit of measure: </xsl:text>
        <xsl:value-of select="$units"/>
        <xsl:text>.</xsl:text>
      </xsl:message>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>length-in-points</refname>
<refpurpose>Returns the size, in points, of a specified length</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>length-in-points</function> template converts a length
specification to points and returns that value as an unqualified
number.
</para>

<caution>
<para>There is no way for the template to infer the size of an
<literal>em</literal>. It relies on the default <parameter>em.size</parameter>
which is initially <literal>10</literal> (for 10pt).</para>

<para>Similarly, converting pixels to points relies on the
<parameter>pixels.per.inch</parameter> parameter which is initially
<literal>90</literal>.
</para>
</caution>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="length-in-points">
  <xsl:param name="length" select="'0pt'"/>
  <xsl:param name="em.size" select="10"/>
  <xsl:param name="pixels.per.inch" select="90"/>

  <xsl:variable name="magnitude">
    <xsl:call-template name="length-magnitude">
      <xsl:with-param name="length" select="$length"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="units">
    <xsl:value-of select="substring($length, string-length($magnitude)+1)"/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="$units = 'pt'">
      <xsl:value-of select="$magnitude"/>
    </xsl:when>
    <xsl:when test="$units = 'cm'">
      <xsl:value-of select="$magnitude div 2.54 * 72.0"/>
    </xsl:when>
    <xsl:when test="$units = 'mm'">
      <xsl:value-of select="$magnitude div 25.4 * 72.0"/>
    </xsl:when>
    <xsl:when test="$units = 'in'">
      <xsl:value-of select="$magnitude * 72.0"/>
    </xsl:when>
    <xsl:when test="$units = 'pc'">
      <xsl:value-of select="$magnitude * 12.0"/>
    </xsl:when>
    <xsl:when test="$units = 'px'">
      <xsl:value-of select="$magnitude div $pixels.per.inch * 72.0"/>
    </xsl:when>
    <xsl:when test="$units = 'em'">
      <xsl:value-of select="$magnitude * $em.size"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:message>
        <xsl:text>Unrecognized unit of measure: </xsl:text>
        <xsl:value-of select="$units"/>
        <xsl:text>.</xsl:text>
      </xsl:message>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>pi-attribute</refname>
<refpurpose>Extract a pseudo-attribute from a PI</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>pi-attribute</function> template extracts a pseudo-attribute
from a processing instruction. For example, given the PI
<quote><literal>&lt;?foo bar="1" baz='red'?&gt;</literal></quote>,</para>
<programlisting>&lt;xsl:call-template name="pi-attribute"&gt;
  &lt;xsl:with-param name="pis" select="processing-instruction('foo')"/&gt;
  &lt;xsl:with-param name="attribute" select="'baz'"/&gt;
&lt;/xsl:call-template&gt;</programlisting>
<para>will return <quote>red</quote>. This template returns the first matching
attribute that it finds. Presented with processing instructions that
contain badly formed pseudo-attributes (missing or unbalanced quotes,
for example), the template may silently return erroneous results.</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="pi-attribute">
  <xsl:param name="pis" select="processing-instruction('BOGUS_PI')"/>
  <xsl:param name="attribute">filename</xsl:param>
  <xsl:param name="count">1</xsl:param>

  <xsl:choose>
    <xsl:when test="$count&gt;count($pis)">
      <!-- not found -->
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="pi">
        <xsl:value-of select="$pis[$count]"/>
      </xsl:variable>
      <xsl:variable name="pivalue">
        <xsl:value-of select="concat(' ', normalize-space($pi))"/>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="contains($pivalue,concat(' ', $attribute, '='))">
          <xsl:variable name="rest" select="substring-after($pivalue,concat(' ', $attribute,'='))"/>
          <xsl:variable name="quote" select="substring($rest,1,1)"/>
          <xsl:value-of select="substring-before(substring($rest,2),$quote)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="pi-attribute">
            <xsl:with-param name="pis" select="$pis"/>
            <xsl:with-param name="attribute" select="$attribute"/>
            <xsl:with-param name="count" select="$count + 1"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>lookup.key</refname>
<refpurpose>Retrieve the value associated with a particular key in a table</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>Given a table of space-delimited key/value pairs,
the <function>lookup.key</function> template extracts the value associated
with a particular key.</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="lookup.key">
  <xsl:param name="key" select="''"/>
  <xsl:param name="table" select="''"/>

  <xsl:if test="contains($table, ' ')">
    <xsl:choose>
      <xsl:when test="substring-before($table, ' ') = $key">
        <xsl:variable name="rest" select="substring-after($table, ' ')"/>
        <xsl:choose>
          <xsl:when test="contains($rest, ' ')">
            <xsl:value-of select="substring-before($rest, ' ')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$rest"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="lookup.key">
          <xsl:with-param name="key" select="$key"/>
          <xsl:with-param name="table" select="substring-after(substring-after($table,' '), ' ')"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<doc:refentry><refnamediv>
<refname>xpath.location</refname>
<refpurpose>Calculate the XPath child-sequence to the current node</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>xpath.location</function> template calculates the
absolute path from the root of the tree to the current element node.
</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="xpath.location">
  <xsl:param name="node" select="."/>
  <xsl:param name="path" select="''"/>

  <xsl:variable name="next.path">
    <xsl:value-of select="local-name($node)"/>
    <xsl:if test="$path != ''">/</xsl:if>
    <xsl:value-of select="$path"/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test="$node/parent::*">
      <xsl:call-template name="xpath.location">
        <xsl:with-param name="node" select="$node/parent::*"/>
        <xsl:with-param name="path" select="$next.path"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>/</xsl:text>
      <xsl:value-of select="$next.path"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>comment-escape-string</refname>
<refpurpose>Prepare a string for inclusion in an XML comment</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>comment-escape-string</function> template returns a string
that has been transformed so that it can safely be output as an XML comment.
Internal occurrences of "--" will be replaced with "- -" and a leading and/or
trailing space will be added to the string, if necessary.</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="comment-escape-string">
  <xsl:param name="string" select="''"/>

  <xsl:if test="starts-with($string, '-')">
    <xsl:text> </xsl:text>
  </xsl:if>

  <xsl:call-template name="comment-escape-string.recursive">
    <xsl:with-param name="string" select="$string"/>
  </xsl:call-template>

  <xsl:if test="substring($string, string-length($string), 1) = '-'">
    <xsl:text> </xsl:text>
  </xsl:if>
</xsl:template>

<doc:refentry><refnamediv>
<refname>comment-escape-string.recursive</refname>
<refpurpose>Internal function used by comment-escape-string</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The <function>comment-escape-string.recursive</function> template is used
by <function>comment-escape-string</function>.</para>

<programlisting/>
</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="comment-escape-string.recursive">
  <xsl:param name="string" select="''"/>
  <xsl:choose>
    <xsl:when test="contains($string, '--')">
      <xsl:value-of select="substring-before($string, '--')"/>
      <xsl:value-of select="'- -'"/>
      <xsl:call-template name="comment-escape-string.recursive">
        <xsl:with-param name="string" select="substring-after($string, '--')"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$string"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>prepend-pad</refname>
<refpurpose>Right-pad a string out to a certain length</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>This function takes string <parameter>padVar</parameter> and
pads it out to the string-length <parameter>length</parameter>, using
string <parameter>padChar</parameter> (a space character by default)
as the padding string (note that <parameter>padChar</parameter> can be
a string; it is not limited to just being a single character).</para>

  <note>
    <para>This function is a copy of Nate Austin's
    <function>prepend-pad</function> function in the <ulink url="http://www.dpawson.co.uk/xsl/sect2/padding.html">Padding
    Content</ulink> section of Dave Pawson's <ulink url="http://www.dpawson.co.uk/xsl/index.html">XSLT
    FAQ</ulink>.</para>
  </note>

<programlisting/>

</refsect1>
</doc:refentry>
  <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="prepend-pad">    
  <!-- recursive template to right justify and prepend-->
  <!-- the value with whatever padChar is passed in   -->
    <xsl:param name="padChar" select="' '"/>
    <xsl:param name="padVar"/>
    <xsl:param name="length"/>
    <xsl:choose>
      <xsl:when test="string-length($padVar) &lt; $length">
        <xsl:call-template name="prepend-pad">
          <xsl:with-param name="padChar" select="$padChar"/>
          <xsl:with-param name="padVar" select="concat($padChar,$padVar)"/>
          <xsl:with-param name="length" select="$length"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="substring($padVar,string-length($padVar) - $length + 1)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<doc:refentry><refnamediv>
<refname>trim.text</refname>
<refpurpose>Trim leading and trailing whitespace from a text node</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>Given a text node, this function trims leading and trailing
whitespace from it and returns the trimmed contents.</para>

<programlisting/>

</refsect1>
</doc:refentry>

  <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="trim.text">
    <xsl:param name="contents" select="."/>
    <xsl:variable name="contents-left-trimmed">
      <xsl:call-template name="trim-left">
        <xsl:with-param name="contents" select="$contents"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="contents-trimmed">
      <xsl:call-template name="trim-right">
        <xsl:with-param name="contents" select="$contents-left-trimmed"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="$contents-trimmed"/>
  </xsl:template>

  <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="trim-left">
    <xsl:param name="contents"/>
    <xsl:choose>
      <xsl:when test="starts-with($contents,'&#xA;') or                       starts-with($contents,'&#xA;') or                       starts-with($contents,' ') or                       starts-with($contents,'&#x9;')">
        <xsl:call-template name="trim-left">
          <xsl:with-param name="contents" select="substring($contents, 2)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$contents"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="trim-right">
    <xsl:param name="contents"/>
    <xsl:variable name="last-char">
      <xsl:value-of select="substring($contents, string-length($contents), 1)"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="($last-char = '&#xA;') or                       ($last-char = '&#xD;') or                       ($last-char = ' ') or                       ($last-char = '&#x9;')">
        <xsl:call-template name="trim-right">
          <xsl:with-param name="contents" select="substring($contents, 1, string-length($contents) - 1)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$contents"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<doc:refentry><refnamediv>
<refname>str.tokenize.keep.delimiters</refname>
<refpurpose>Tokenize a string while preserving any delimiters</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>Based on the occurrence of one or more delimiter characters,
this function breaks a string into a list of tokens and delimiters,
marking up each of the tokens with a <sgmltag>token</sgmltag> element
and preserving the delimiters as text nodes between the tokens.</para>

<note>
  <para>This function is a very slightly modified version of a
  function from the <ulink url="http://www.exslt.org/">EXSLT
  site</ulink>. The original is available at:

  <blockquote><para><ulink url="http://www.exslt.org/str/functions/tokenize/str.tokenize.template.xsl"/></para></blockquote>

  The <function>str.tokenize.keep.delimiters</function> function
  differs only in that it preserves the delimiters instead of
  discarding them.</para>
</note>

<programlisting/>

</refsect1>
</doc:refentry>

  <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="str.tokenize.keep.delimiters">
    <xsl:param name="string" select="''"/>
    <xsl:param name="delimiters" select="' '"/>
    <xsl:choose>
      <xsl:when test="not($string)"/>
      <xsl:when test="not($delimiters)">
	<xsl:call-template name="str.tokenize.keep.delimiters-characters">
	  <xsl:with-param name="string" select="$string"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="str.tokenize.keep.delimiters-delimiters">
	  <xsl:with-param name="string" select="$string"/>
	  <xsl:with-param name="delimiters" select="$delimiters"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="str.tokenize.keep.delimiters-characters">
    <xsl:param name="string"/>
    <xsl:if test="$string">
      <token><xsl:value-of select="substring($string, 1, 1)"/></token>
      <xsl:call-template name="str.tokenize.keep.delimiters-characters">
	<xsl:with-param name="string" select="substring($string, 2)"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="str.tokenize.keep.delimiters-delimiters">
    <xsl:param name="string"/>
    <xsl:param name="delimiters"/>
    <xsl:variable name="delimiter" select="substring($delimiters, 1, 1)"/>
    <xsl:choose>
      <xsl:when test="not($delimiter)">
	<token><xsl:value-of select="$string"/></token>
      </xsl:when>
      <xsl:when test="contains($string, $delimiter)">
	<xsl:if test="not(starts-with($string, $delimiter))">
	  <xsl:call-template name="str.tokenize.keep.delimiters-delimiters">
	    <xsl:with-param name="string" select="substring-before($string, $delimiter)"/>
	    <xsl:with-param name="delimiters" select="substring($delimiters, 2)"/>
	  </xsl:call-template>
	</xsl:if>
	<!-- output each delimiter -->
	<xsl:value-of select="$delimiter"/>
	<xsl:call-template name="str.tokenize.keep.delimiters-delimiters">
	  <xsl:with-param name="string" select="substring-after($string, $delimiter)"/>
	  <xsl:with-param name="delimiters" select="$delimiters"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="str.tokenize.keep.delimiters-delimiters">
	  <xsl:with-param name="string" select="$string"/>
	  <xsl:with-param name="delimiters" select="substring($delimiters, 2)"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<doc:refentry><refnamediv>
    <refname>apply-string-subst-map</refname>
    <refpurpose>Apply a string-substitution map</refpurpose>
  </refnamediv><refsect1><title>Description</title>

  <para>This function applies a "string substitution" map. Use it when
  you want to do multiple string substitutions on the same target
  content. It reads in two things: <parameter>content</parameter>, the
  content on which to perform the substitution, and
  <parameter>map.contents</parameter>, a node set of
  elements (the names of the elements don't matter), with each element
  having the following attributes:
  <itemizedlist>
    <listitem>
      <simpara><tag class="attribute">oldstring</tag>, a string to
      be replaced</simpara>
    </listitem>
    <listitem>
      <simpara><tag class="attribute">newstring</tag>, a string with
      which to replace <tag class="attribute">oldstring</tag></simpara>
    </listitem>
  </itemizedlist>
  The function uses <parameter>map.contents</parameter> to
  do substitution on <parameter>content</parameter>, and then
  returns the modified contents.</para>

  <note>
    <para>This function is a very slightly modified version of Jeni
    Tennison's <function>replace_strings</function> function in the
    <ulink url="http://www.dpawson.co.uk/xsl/sect2/StringReplace.html#d9351e13">multiple string replacements</ulink> section of Dave Pawson's
    <ulink url="http://www.dpawson.co.uk/xsl/index.html">XSLT
    FAQ</ulink>.</para>

    <para>The <function>apply-string-subst-map</function> function is
    essentially the same function as the
    <function>apply-character-map</function> function; the only
    difference is that in the map that
    <function>apply-string-subst-map</function> expects, <tag class="attribute">oldstring</tag> and <tag class="attribute">newstring</tag> attributes are used instead of
    <tag class="attribute">character</tag> and <tag class="attribute">string</tag> attributes.</para>
  </note>

  <programlisting/>
  </refsect1>
</doc:refentry>
    <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="apply-string-subst-map">
      <xsl:param name="content"/>
      <xsl:param name="map.contents"/>
      <xsl:variable name="replaced_text">
        <xsl:call-template name="string.subst">
          <xsl:with-param name="string" select="$content"/>
          <xsl:with-param name="target" select="$map.contents[1]/@oldstring"/>
          <xsl:with-param name="replacement" select="$map.contents[1]/@newstring"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$map.contents[2]">
          <xsl:call-template name="apply-string-subst-map">
            <xsl:with-param name="content" select="$replaced_text"/>
            <xsl:with-param name="map.contents" select="$map.contents[position() &gt; 1]"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$replaced_text"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

  

<doc:refentry><refnamediv>
    <refname>apply-character-map</refname>
    <refpurpose>Apply an XSLT character map</refpurpose>
  </refnamediv><refsect1><title>Description</title>

  <para>This function applies an <ulink url="http://www.w3.org/TR/xslt20/#character-maps">XSLT character
  map</ulink>; that is, it cause certain individual characters to be
  substituted with strings of one or more characters. It is useful
  mainly for replacing multiple "special" chararacters or symbols in
  the same target content. It reads in two things:
  <parameter>content</parameter>, the content on which to perform the
  substitution, and <parameter>map.contents</parameter>, a
  node set of elements (the names of the elements don't matter), with
  each element having the following attributes:
  <itemizedlist>
    <listitem>
      <simpara><tag class="attribute">character</tag>, a character to
      be replaced</simpara>
    </listitem>
    <listitem>
      <simpara><tag class="attribute">string</tag>, a string with
      which to replace <tag class="attribute">character</tag></simpara>
    </listitem>
  </itemizedlist>
  This function uses <parameter>map.contents</parameter> to
  do substitution on <parameter>content</parameter>, and then returns
  the modified contents.</para>

  <note>
    <para>This function is a very slightly modified version of Jeni
    Tennison's <function>replace_strings</function> function in the
    <ulink url="http://www.dpawson.co.uk/xsl/sect2/StringReplace.html#d9351e13">multiple string replacements</ulink> section of Dave Pawson's
    <ulink url="http://www.dpawson.co.uk/xsl/index.html">XSLT
    FAQ</ulink>.</para>

    <para>The <function>apply-string-subst-map</function> function is
    essentially the same function as the
    <function>apply-character-map</function> function; the only
    difference is that in the map that
    <function>apply-string-subst-map</function> expects, <tag class="attribute">oldstring</tag> and <tag class="attribute">newstring</tag> attributes are used instead of
    <tag class="attribute">character</tag> and <tag class="attribute">string</tag> attributes.</para>
  </note>

  <programlisting/>
  </refsect1>
</doc:refentry>
    <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="apply-character-map">
      <xsl:param name="content"/>
      <xsl:param name="map.contents"/>
      <xsl:variable name="replaced_text">
        <xsl:call-template name="string.subst">
          <xsl:with-param name="string" select="$content"/>
          <xsl:with-param name="target" select="$map.contents[1]/@character"/>
          <xsl:with-param name="replacement" select="$map.contents[1]/@string"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$map.contents[2]">
          <xsl:call-template name="apply-character-map">
            <xsl:with-param name="content" select="$replaced_text"/>
            <xsl:with-param name="map.contents" select="$map.contents[position() &gt; 1]"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$replaced_text"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

  

<doc:refentry><refnamediv>
<refname>read-character-map</refname>
<refpurpose>Read in all or part of an XSLT character map</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>The XSLT 2.0 specification describes <ulink url="http://www.w3.org/TR/xslt20/#character-maps">character
maps</ulink> and explains how they may be used to allow a specific
character appearing in a text or attribute node in a final results
tree to be substituted by a specified string of characters during
serialization. The <function>read-character-map</function> function
provides a means for reading and using character maps with XSLT
1.0-based tools.</para>

<para>It reads the character-map contents from
<parameter>uri</parameter> (in full or in part, depending on the value
of the <parameter>use.subset</parameter> parameter), then passes those
contents to the <function>apply-character-map</function> function,
along with <parameter>content</parameter>, the data on which to
perform the character substition.</para>

<para>Using the character map "in part" means that it uses only those
<tag>output-character</tag> elements that match the XPATH expression
given in the value of the <parameter>subset.profile</parameter>
parameter. The current implementation of that capability here relies
on the <function>evaluate</function> extension XSLT function.</para>

<programlisting/>
</refsect1>
</doc:refentry>
  <xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="read-character-map">
    <xsl:param name="use.subset"/>
    <xsl:param name="subset.profile"/>
    <xsl:param name="uri"/>
    <xsl:choose>
      <xsl:when test="$use.subset != 0">
        <!-- use a subset of the character map instead of the full map -->
        <xsl:choose>
          <!-- xsltproc and Xalan both support dyn:evaluate() -->
          <xsl:when test="function-available('dyn:evaluate')">
            <xsl:copy-of select="document($uri)//*[local-name()='output-character']                                  [dyn:evaluate($subset.profile)]"/>
          </xsl:when>
          <!-- Saxon has its own evaluate() & doesn't support dyn:evaluate() -->
          <xsl:when test="function-available('saxon:evaluate')">
            <xsl:copy-of select="document($uri)//*[local-name()='output-character']                                  [saxon:evaluate($subset.profile)]"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message terminate="yes">
Error: To process character-map subsets, you must use an XSLT engine
that supports the evaluate() XSLT extension function. Your XSLT engine
does not support it.
</xsl:message>
          </xsl:otherwise>
        </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <!-- value of $use.subset is non-zero, so use the full map -->
        <xsl:copy-of select="document($uri)//*[local-name()='output-character']"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<doc:refentry><refnamediv>
<refname>count.uri.path.depth</refname>
<refpurpose>Count the number of path components in a relative URI</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>This function counts the number of path components in a relative URI.</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="count.uri.path.depth">
  <xsl:param name="filename" select="''"/>
  <xsl:param name="count" select="0"/>

  <xsl:choose>
    <xsl:when test="contains($filename, '/')">
      <xsl:call-template name="count.uri.path.depth">
        <xsl:with-param name="filename" select="substring-after($filename, '/')"/>
        <xsl:with-param name="count" select="$count + 1"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$count"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<doc:refentry><refnamediv>
<refname>trim.common.uri.paths</refname>
<refpurpose>Trim common leading path components from a relative URI</refpurpose>
</refnamediv><refsect1><title>Description</title>

<para>This function trims common leading path components from a relative URI.</para>

<programlisting/>

</refsect1>
</doc:refentry>
<xsl:template xmlns:dyn="http://exslt.org/dynamic" xmlns:saxon="http://icl.com/saxon" name="trim.common.uri.paths">
  <xsl:param name="uriA" select="''"/>
  <xsl:param name="uriB" select="''"/>
  <xsl:param name="return" select="'A'"/>

  <xsl:choose>
    <xsl:when test="contains($uriA, '/') and contains($uriB, '/')                     and substring-before($uriA, '/') = substring-before($uriB, '/')">
      <xsl:call-template name="trim.common.uri.paths">
        <xsl:with-param name="uriA" select="substring-after($uriA, '/')"/>
        <xsl:with-param name="uriB" select="substring-after($uriB, '/')"/>
        <xsl:with-param name="return" select="$return"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="$return = 'A'">
          <xsl:value-of select="$uriA"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$uriB"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
</xsl:stylesheet>