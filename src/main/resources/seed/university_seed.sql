START TRANSACTION;

-- university upsert (MySQL 8 deprecation 대응)
INSERT INTO university (name, acronym, region, is_active, created_at, updated_at)
VALUES
('부산대학교', NULL, NULL, TRUE, NOW(), NOW()),
('국립부경대학교', NULL, NULL, TRUE, NOW(), NOW()),
('국립한국해양대학교', NULL, NULL, TRUE, NOW(), NOW()),
('동아대학교', NULL, NULL, TRUE, NOW(), NOW()),
('경성대학교', NULL, NULL, TRUE, NOW(), NOW()),
('동의대학교', NULL, NULL, TRUE, NOW(), NOW()),
('신라대학교', NULL, NULL, TRUE, NOW(), NOW()),
('부산외국어대학교', NULL, NULL, TRUE, NOW(), NOW()),
('동서대학교', NULL, NULL, TRUE, NOW(), NOW()),
('동명대학교', NULL, NULL, TRUE, NOW(), NOW()),
('부산가톨릭대학교', NULL, NULL, TRUE, NOW(), NOW()),
('고신대학교', NULL, NULL, TRUE, NOW(), NOW()),
('부산교육대학교', NULL, NULL, TRUE, NOW(), NOW()),
('영산대학교', NULL, NULL, TRUE, NOW(), NOW()),
('부산디지털대학교', NULL, NULL, TRUE, NOW(), NOW()),
('울산대학교', NULL, NULL, TRUE, NOW(), NOW()),
('울산과학기술원', NULL, NULL, TRUE, NOW(), NOW()),
('울산과학대학교', NULL, NULL, TRUE, NOW(), NOW()),
('경상국립대학교', NULL, NULL, TRUE, NOW(), NOW()),
('국립창원대학교', NULL, NULL, TRUE, NOW(), NOW()),
('인제대학교', NULL, NULL, TRUE, NOW(), NOW()),
('경남대학교', NULL, NULL, TRUE, NOW(), NOW()),
('진주교육대학교', NULL, NULL, TRUE, NOW(), NOW()),
('한국폴리텍대학', NULL, NULL, TRUE, NOW(), NOW()),
('마산대학교', NULL, NULL, TRUE, NOW(), NOW()),
('경남정보대학교', NULL, NULL, TRUE, NOW(), NOW()),
('동의과학대학교', NULL, NULL, TRUE, NOW(), NOW()),
('부산과학기술대학교', NULL, NULL, TRUE, NOW(), NOW()),
('부산경상대학교', NULL, NULL, TRUE, NOW(), NOW()),
('부산여자대학교', NULL, NULL, TRUE, NOW(), NOW()),
('대동대학교', NULL, NULL, TRUE, NOW(), NOW()),
('부산예술대학교', NULL, NULL, TRUE, NOW(), NOW()),
('춘해보건대학교', NULL, NULL, TRUE, NOW(), NOW()),
('연암공과대학교', NULL, NULL, TRUE, NOW(), NOW()),
('진주보건대학교', NULL, NULL, TRUE, NOW(), NOW()),
('김해대학교', NULL, NULL, TRUE, NOW(), NOW()),
('거제대학교', NULL, NULL, TRUE, NOW(), NOW()),
('동원과학기술대학교', NULL, NULL, TRUE, NOW(), NOW()),
('창원문성대학교', NULL, NULL, TRUE, NOW(), NOW()),
('한국승강기대학교', NULL, NULL, TRUE, NOW(), NOW()),
('경남도립거창대학', NULL, NULL, TRUE, NOW(), NOW()),
('경남도립남해대학', NULL, NULL, TRUE, NOW(), NOW())
AS new
ON DUPLICATE KEY UPDATE
  acronym = new.acronym,
  region = new.region,
  is_active = new.is_active,
  updated_at = NOW();

-- university_domain upsert (VALUES() 미사용)
INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'pusan.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'pnu.edu', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'pukyong.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '국립부경대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '국립부경대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'pknu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '국립부경대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '국립부경대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'g.kmou.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '국립한국해양대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '국립한국해양대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'kmou.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '국립한국해양대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '국립한국해양대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'donga.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '동아대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '동아대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'kyungsung.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '경성대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '경성대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'ks.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '경성대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '경성대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'deu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '동의대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '동의대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'sillain.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '신라대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '신라대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'office.bufs.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산외국어대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산외국어대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'dsu.dongseo.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '동서대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '동서대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'dongseo.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '동서대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '동서대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'g.tu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '동명대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '동명대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'o365.tu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '동명대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '동명대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'cup.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산가톨릭대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산가톨릭대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'kosin.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '고신대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '고신대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'kucm.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '고신대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '고신대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'g.bnue.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산교육대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산교육대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'bnue.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산교육대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산교육대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'ysu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '영산대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '영산대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'bdu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산디지털대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산디지털대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'gclass.bdu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산디지털대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산디지털대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'mail.ulsan.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '울산대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '울산대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'ulsan.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '울산대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '울산대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'unist.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '울산과학기술원'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '울산과학기술원'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'gnu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '경상국립대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '경상국립대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'gs.cwnu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '국립창원대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '국립창원대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'go.inje.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '인제대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '인제대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'o365.inje.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '인제대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '인제대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'o365.kyungnam.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '경남대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '경남대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'kyungnam.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '경남대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '경남대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'cue.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '진주교육대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '진주교육대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'kopo.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '한국폴리텍대학'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '한국폴리텍대학'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'o365.masan.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '마산대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '마산대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'kit.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '경남정보대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '경남정보대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'dit.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '동의과학대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '동의과학대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'g.dit.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '동의과학대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '동의과학대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'bist.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산과학기술대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산과학기술대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'o365.bist.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산과학기술대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산과학기술대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'bsks.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산경상대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산경상대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'bwc.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산여자대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산여자대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'daedong.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '대동대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '대동대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'pusanarts.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '부산예술대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '부산예술대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'go.uc.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '울산과학대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '울산과학대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'o365.uc.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '울산과학대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '울산과학대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'ch.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '춘해보건대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '춘해보건대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'o365.ch.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '춘해보건대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '춘해보건대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'yc.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '연암공과대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '연암공과대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'jhc.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '진주보건대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '진주보건대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'gsc.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '김해대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '김해대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'koje.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '거제대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '거제대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'dist.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '동원과학기술대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '동원과학기술대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'cmu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '창원문성대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '창원문성대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'office.cmu.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '창원문성대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '창원문성대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'klc.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '한국승강기대학교'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '한국승강기대학교'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'gc.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '경남도립거창대학'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '경남도립거창대학'),
  category = 'STUDENT',
  updated_at = NOW();

INSERT INTO university_domain (university_id, domain, category, created_at, updated_at)
SELECT u.id, 'namhae.ac.kr', 'STUDENT', NOW(), NOW()
FROM university u
WHERE u.name = '경남도립남해대학'
ON DUPLICATE KEY UPDATE
  university_id = (SELECT id FROM university WHERE name = '경남도립남해대학'),
  category = 'STUDENT',
  updated_at = NOW();

COMMIT;
