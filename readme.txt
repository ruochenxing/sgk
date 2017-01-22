查询重复数据
select * from account where id in (select id from account where source='163' group by email having count(email)>1)