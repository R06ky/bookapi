# BOOKAPI
* Get a book information from dangdang.com, jd.com, douban.com
* Please change `java.policy`, add `permission java.security.AllPermission;`
* run > `java -jar build/libs/bookapi-0.1.0.jar`

## application.properties
- **server.port** is the API server port, default is 8090
## logback.xml
- please set the `@LOG_HOME` for log file

## Three APIs
###### get simple book by order URL, 
* it return a book once getting response from (dangdang.com->jd.com->doubnan.com);
* <http://localhost:8090/book?name=@name>
* <http://localhost:8090/book?isbn=@isbn>

###### get simple book by target URL, 
* @webtag = `dd`,`jd`,`db`
* <http://localhost:8090/book?isbn=@isbn&target=@webtag>
* <http://localhost:8090/book?name=@name&target=@webtag>

###### get combined book from `dangdang.com`, `jd.com`, `douban.com`
* <http://localhost:8090/cbook?name=@name>
* <http://localhost:8090/cbook?isbn=@isbn>

###### JSON
```
{"title":"中国传统文化经典选读 元人杂剧选","isbn10":"7020111416","isbn13":"9787020111411","images":["http://img3x4.ddimg.cn/74/4/1464336794-1_w_2.jpg","http://img3x4.ddimg.cn/74/4/1464336794-1_x_2.jpg"],"author":["顾学颉"],"translator":[],"publisher":"人民文学出版社","pubdate":"2016-1-1","authorIntro":"","summary":"<span style=\"font-size: 16px; line-height: 24px;\">　　《元人杂剧选》是从现存的可信为元人之作的一百三十多种杂剧中挑选出来的。注者共选了十六个剧本。入选各剧，均以明人臧懋循所编《元曲选》为底本，因为比较完善。但也偶有改动失误之处，均据其他明刊本作了补正，并一一加了说明。对所选各剧，注者做了比较详尽的注释。此外为考虑到不同读者群的需求，此版本特排印成大字版式以方便读者诵读览看</span>","catalog":"<p>感天动地窦娥冤关汉卿</p><p>赵盼儿风月救风尘关汉卿</p><p>唐明皇秋夜梧桐雨白仁甫</p><p>破幽梦孤雁汉宫秋马致远</p><p>梁山泊李逵负荆康进之</p><p>沙门岛张生煮海李好古</p><p>鲁大夫秋胡戏妻石君宝</p><p>赵氏孤儿大报仇纪君祥</p><p>张孔目智勘魔合罗孟汉卿</p><p>便宜行事虎头牌李直夫</p><p>相国寺公孙合汗衫张国宾</p><p>迷青琐倩女离魂郑德辉</p><p>东堂老劝破家子弟秦简夫</p><p>包待制智赚生金阁无名氏（或作武汉臣）</p><p>风雨像生货郎旦无名氏</p><p>包待制陈州粜米无名氏</p>","categroy":"图书|文学|文集"}
```
