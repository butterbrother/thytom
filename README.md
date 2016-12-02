# thytom
Database client for bulk export data. [Страница на русском](https://github.com/butterbrother/thytom/wiki/ru:README)  

This client processes a set of requests from sql-files. Each query can be used substitution template which in turn receives from another file ("substitution file"). Each request will be executed sequentially for each substitution. The result of each request remains in own file.

<a href="https://scan.coverity.com/projects/butterbrother-thytom">
  <img alt="Coverity Scan Build Status"
       src="https://scan.coverity.com/projects/11036/badge.svg"/>
</a>  

## Usage  
### Settings
Connection and encoding settings contains in configuration file, conf/thyton.properties.  
Database connection settings strongly required and must be set. But it can be empty (then it not used):  
`# Database JDBC URL. For sample`  
`db.url=jdbc:oracle:thin:@some.large.db:1521:db`  
`# Database user login.`  
`db.login=username`  
`# Database user password.`  
`db.password=password`  
`# Database JDBC driver name.`  
`db.driver=org.sqlite.JDBC`  

File encoding parameters.  
All parameters optional.  
Default - UTF-8 (if not set)  
`# Encoding of .sql-files`  
`#file.sql.encoding=UTF-8`  
`# Encoding of substition file`  
`#file.subs.encoding=UTF-8`  
`# Encoding of result files`  
`#file.result.encoding=UTF-8`  

### Command line usage
<table>
<thead>
	<tr>
		<th>Short</th>
		<th>Long</th>
		<th>Argument</th>
		<th>Description</th>
	</tr>
</thead>
<tbody>
	<tr>
		<td>-h</td>
		<td>--help</td>
		<td></td>
		<td>Show same command line usage help.</td>
	</tr>
	<tr>
	  <td>-d</td>
	  <td>--col-delim</td>
	  <td>delimiter</td>
	  <td>
	    Alternative separator of columns in
      results of requests. The semicolon is by
      default used.
    </td
  </tr>
  <tr>
    <td>-s</td>
    <td>--title-head</td>
    <td></td>
    <td>
      Display headers in the results of
      queries. By default, headings are not
      displayed.
    </td>
  </tr>
  <tr>
    <td>-e</td>
    <td>--head-per-line</td>
    <td></td>
    <td>
      Show headers on each line. i.e. instead of one general header:<br>
      <tt>col1;col2;col3</tt><br>
      <tt>AAA;BBB;CCC</tt><br>
      <tt>DDD;EEE;FFF</tt><br>
      <br>
      each row header will be displayed in front of each field:<br>
      <tt>col1:AAA;col2:BBB;col3:CCC</tt><br>
      <tt>col1:DDD;col2:EEE;col3:FFF</tt><br>
      <br>
      This parameter automatically disable "--title-head".
    </td>
  </tr>
  <tr>
    <td>-t</td>
    <td>--per-line-sep</td>
    <td>delimiter</td>
    <td>
      Alternate separator of headers and
      fields, if switch onshow headers on each
      line. The colon is by default used.
      This parameter automatically activate
      "--head-per-line".
    </td>
  </tr>
  <tr>
    <td>-f</td>
    <td>--subs-file</td>
    <td>file name</td>
    <td>
      File with the data for substitution into
      SQL queries. Each parameter from this
      file will be in turn inserted into the
      SQL query and executed if in request
      there is a template {PARAM}.<br>
      For sample file:<br>
      <tt>AAA</tt><br>
      <tt>BBB</tt><br>
      SQL query:<br>
      <tt>select * from data where owner={PARAM}</tt><br><br>
      If the file is not set, but there is a
      template {PARAM} in queries, then an
      empty string will be substituted for the
      {PARAM} (or anothercustom) template.
    </td>
  </tr>
  <tr>
    <td>-a</td>
    <td>--cust-templ</td>
    <td></td>
    <td>
      Alternative template for the substitution
      in the parameter file. It assumes that
      uses one or more of their own, instead of
      a basic template {PARAM}. In this case
      templates - column headings in the file
      with substitutions (and file - is table).<br>
      For sample template file:<br>
      <tt>col1;col2</tt><br>
      <tt>AAA;BBB</tt><br>
      Sql query:<br>
      <tt>Select * from data where owner={col1} and key={col2}</tt><br><br>
      This parameter depend of "--subs-file". And
      is not activated, if it is not specified.
    </td>
  </tr>
  <tr>
    <td>-l</td>
    <td>--templ-sep</td>
    <td>separator</td>
    <td>
      Alternative separator of columns in the
      file with substitutions andcustom
      substitution templates.
      The semicolon is used by default.
      This parameter automatically activate
      "--subs-file" and "--cust-templ". And of
      course it depend of "--subs-file".
    </td>
  </tr>
  <tr>
    <td>-n</td>
    <td>--nulls</td>
    <td></td>
    <td>
      Show null as "null" in results.
      By default show empty string.
    </td>
  </tr>
  <tr>
    <td>-w</td>
    <td>--trim-data</td>
    <td></td>
    <td>
      Remove the spaces before and after values
      for each cell results (trim).
    </td>
  </tr>
  <tr>
    <td>-c</td>
    <td>--trim-subs</td>
    <td></td>
    <td>
      Remove the spaces before and after values
      of the parameters in the file
      substitution (trim)
    </td>
  </tr>
</tbody>
</table>

### Database drivers  
Jars of database drivers put info "lib" directory. Its automatically activate and use on application startup.  
Name of DB driver set in configuration file, option - `db.driver`.
