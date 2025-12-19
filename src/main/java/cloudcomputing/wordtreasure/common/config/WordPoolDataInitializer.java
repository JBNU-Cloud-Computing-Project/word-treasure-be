package cloudcomputing.wordtreasure.common.config;

import cloudcomputing.wordtreasure.model.game.entity.Difficulty;
import cloudcomputing.wordtreasure.model.game.entity.WordPool;
import cloudcomputing.wordtreasure.model.game.repository.WordPoolRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 로컬 개발 환경(H2)에서 WordPool 초기 데이터를 자동 로딩하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"local", "prod"})  // local, prod 프로필에서만 동작
public class WordPoolDataInitializer {

    private final WordPoolRepository wordPoolRepository;

    @PostConstruct
    @Transactional
    public void init() {
        // 이미 데이터가 있으면 스킵
        if (wordPoolRepository.count() > 0) {
            log.info("✅ WordPool 데이터가 이미 존재합니다. 초기화를 건너뜁니다. ({}개)",
                    wordPoolRepository.count());
            return;
        }

        log.info("╔════════════════════════════════════════════╗");
        log.info("║  WordPool 초기 데이터 로딩 시작           ║");
        log.info("╚════════════════════════════════════════════╝");

        loadSampleWords();

        long totalCount = wordPoolRepository.count();
        log.info("✅ WordPool 초기 데이터 로딩 완료: 총 {} 개", totalCount);
        log.info("   - EASY: {}개", countByDifficulty(Difficulty.EASY));
        log.info("   - MEDIUM: {}개", countByDifficulty(Difficulty.MEDIUM));
        log.info("   - HARD: {}개", countByDifficulty(Difficulty.HARD));
    }

    private void loadSampleWords() {
        List<WordPool> words = new ArrayList<>();

        // EASY 단어들 (30개)
        words.addAll(createEasyWords());

        // MEDIUM 단어들 (50개)
        words.addAll(createMediumWords());

        // HARD 단어들 (20개)
        words.addAll(createHardWords());

        wordPoolRepository.saveAll(words);
    }

    private List<WordPool> createEasyWords() {
        List<WordPool> words = new ArrayList<>();

        // 감정 (5개)
        words.add(createWord("행복", "기쁘고 즐거운 마음 상태", Difficulty.EASY, "감정",
                "오늘은 정말 행복한 하루였어요.", null));
        words.add(createWord("슬픔", "마음이 아프고 우울한 감정", Difficulty.EASY, "감정",
                "그 소식을 듣고 깊은 슬픔에 빠졌다.", null));
        words.add(createWord("화남", "기분이 상해서 분노하는 상태", Difficulty.EASY, "감정",
                "부당한 대우에 화가 났다.", null));
        words.add(createWord("두려움", "무서워서 겁나는 마음", Difficulty.EASY, "감정",
                "어둠에 대한 두려움이 있다.", null));
        words.add(createWord("기쁨", "즐겁고 유쾌한 감정", Difficulty.EASY, "감정",
                "작은 것에서도 기쁨을 찾는다.", null));

        // 동물 (5개)
        words.add(createWord("고양이", "집에서 기르는 귀여운 애완동물", Difficulty.EASY, "동물",
                "우리 집 고양이는 정말 귀엽다.", null));
        words.add(createWord("강아지", "충성스러운 애완동물", Difficulty.EASY, "동물",
                "강아지를 산책시키는 것이 일과다.", null));
        words.add(createWord("토끼", "귀가 긴 깡충깡충 뛰는 동물", Difficulty.EASY, "동물",
                "토끼는 당근을 좋아한다.", null));
        words.add(createWord("사자", "백수의 왕이라 불리는 맹수", Difficulty.EASY, "동물",
                "사자는 초원의 왕이다.", null));
        words.add(createWord("물고기", "물속에서 사는 생물", Difficulty.EASY, "동물",
                "수족관에 예쁜 물고기가 많다.", null));

        // 음식 (5개)
        words.add(createWord("피자", "치즈와 토핑을 얹은 이탈리아 음식", Difficulty.EASY, "음식",
                "주말에는 피자를 시켜먹는다.", null));
        words.add(createWord("김치", "한국의 전통 발효 음식", Difficulty.EASY, "음식",
                "김치는 한국인의 소울푸드다.", null));
        words.add(createWord("초밥", "생선을 올린 일본 음식", Difficulty.EASY, "음식",
                "초밥을 먹으러 일식집에 갔다.", null));
        words.add(createWord("햄버거", "빵 사이에 패티를 넣은 음식", Difficulty.EASY, "음식",
                "햄버거와 감자튀김을 주문했다.", null));
        words.add(createWord("라면", "면을 끓여 먹는 간편 음식", Difficulty.EASY, "음식",
                "야식으로 라면을 끓여먹었다.", null));

        // 물건 (5개)
        words.add(createWord("연필", "글씨를 쓰는 필기도구", Difficulty.EASY, "물건",
                "연필로 그림을 그렸다.", null));
        words.add(createWord("시계", "시간을 알려주는 도구", Difficulty.EASY, "물건",
                "벽에 걸린 시계를 봤다.", null));
        words.add(createWord("책상", "공부나 작업을 하는 가구", Difficulty.EASY, "물건",
                "책상을 깨끗이 정리했다.", null));
        words.add(createWord("컴퓨터", "전자 계산기", Difficulty.EASY, "물건",
                "컴퓨터로 작업을 한다.", null));
        words.add(createWord("가방", "물건을 담아 들고 다니는 용품", Difficulty.EASY, "물건",
                "새 가방을 샀다.", null));

        // 자연 (5개)
        words.add(createWord("바다", "넓고 깊은 소금물 지역", Difficulty.EASY, "자연",
                "여름에 바다로 놀러갔다.", null));
        words.add(createWord("산", "높이 솟아오른 땅", Difficulty.EASY, "자연",
                "주말에 산에 등산을 간다.", null));
        words.add(createWord("꽃", "식물의 아름다운 부분", Difficulty.EASY, "자연",
                "정원에 꽃이 활짝 피었다.", null));
        words.add(createWord("나무", "땅에 뿌리를 내린 식물", Difficulty.EASY, "자연",
                "큰 나무 아래에서 쉬었다.", null));
        words.add(createWord("하늘", "지구를 둘러싼 공간", Difficulty.EASY, "자연",
                "오늘 하늘이 정말 맑다.", null));

        // 행동 (5개)
        words.add(createWord("달리기", "빠르게 뛰는 행동", Difficulty.EASY, "행동",
                "매일 아침 달리기를 한다.", null));
        words.add(createWord("점프", "뛰어오르는 동작", Difficulty.EASY, "행동",
                "높이 점프를 했다.", null));
        words.add(createWord("웃음", "기쁠 때 짓는 표정과 소리", Difficulty.EASY, "행동",
                "재미있는 영화에 웃음이 나왔다.", null));
        words.add(createWord("걷기", "천천히 이동하는 행동", Difficulty.EASY, "행동",
                "공원에서 걷기를 즐긴다.", null));
        words.add(createWord("노래", "목소리로 음을 내는 행위", Difficulty.EASY, "행동",
                "샤워하면서 노래를 부른다.", null));

        return words;
    }

    private List<WordPool> createMediumWords() {
        List<WordPool> words = new ArrayList<>();

        // 게임 (10개)
        words.add(createWord("마피아", "시민과 마피아로 나뉘어 진행하는 추리 게임", Difficulty.MEDIUM, "게임",
                "친구들과 마피아 게임을 했다.", null));
        words.add(createWord("체스", "두 명이 말을 움직여 왕을 잡는 보드게임", Difficulty.MEDIUM, "게임",
                "체스는 전략이 중요하다.", null));
        words.add(createWord("포커", "카드로 족보를 만드는 도박 게임", Difficulty.MEDIUM, "게임",
                "포커에서는 블러핑이 중요하다.", null));
        words.add(createWord("숨바꼭질", "숨는 사람과 찾는 사람으로 나뉘는 놀이", Difficulty.MEDIUM, "게임",
                "어릴 때 숨바꼭질을 자주 했다.", null));
        words.add(createWord("퍼즐", "조각을 맞춰 완성하는 게임", Difficulty.MEDIUM, "게임",
                "1000피스 퍼즐을 완성했다.", null));
        words.add(createWord("윷놀이", "윷을 던져 말을 움직이는 한국 전통 놀이", Difficulty.MEDIUM, "게임",
                "설날에 가족과 윷놀이를 한다.", null));
        words.add(createWord("보드게임", "판 위에서 말이나 카드로 하는 게임", Difficulty.MEDIUM, "게임",
                "보드게임 카페에서 시간을 보냈다.", null));
        words.add(createWord("레이싱", "속도를 겨루는 경주", Difficulty.MEDIUM, "게임",
                "레이싱 게임에서 1등을 했다.", null));
        words.add(createWord("야구", "9명씩 두 팀이 공을 치고 받는 운동", Difficulty.MEDIUM, "게임",
                "야구 경기를 관람했다.", null));
        words.add(createWord("축구", "발로 공을 차서 골을 넣는 운동", Difficulty.MEDIUM, "게임",
                "축구는 세계적인 스포츠다.", null));

        // 직업 (10개)
        words.add(createWord("의사", "병을 진단하고 치료하는 사람", Difficulty.MEDIUM, "직업",
                "의사가 되려면 오랜 공부가 필요하다.", null));
        words.add(createWord("선생님", "학생을 가르치는 사람", Difficulty.MEDIUM, "직업",
                "선생님께 감사 인사를 드렸다.", null));
        words.add(createWord("요리사", "음식을 만드는 전문가", Difficulty.MEDIUM, "직업",
                "유명한 요리사의 레스토랑에 갔다.", null));
        words.add(createWord("경찰", "범죄를 예방하고 단속하는 사람", Difficulty.MEDIUM, "직업",
                "경찰이 교통을 정리하고 있다.", null));
        words.add(createWord("소방관", "불을 끄고 사람을 구조하는 사람", Difficulty.MEDIUM, "직업",
                "소방관은 용감한 직업이다.", null));
        words.add(createWord("프로그래머", "컴퓨터 프로그램을 만드는 사람", Difficulty.MEDIUM, "직업",
                "프로그래머로 일하고 있다.", null));
        words.add(createWord("디자이너", "시각적 디자인을 하는 사람", Difficulty.MEDIUM, "직업",
                "디자이너가 로고를 새로 만들었다.", null));
        words.add(createWord("기자", "뉴스를 취재하고 보도하는 사람", Difficulty.MEDIUM, "직업",
                "기자가 인터뷰를 요청했다.", null));
        words.add(createWord("변호사", "법률 사건을 처리하는 전문가", Difficulty.MEDIUM, "직업",
                "변호사와 상담을 했다.", null));
        words.add(createWord("건축가", "건물을 설계하는 사람", Difficulty.MEDIUM, "직업",
                "유명한 건축가가 설계한 건물이다.", null));

        // 추상 개념 (10개)
        words.add(createWord("자유", "구속이나 제약 없이 자기 뜻대로 하는 것", Difficulty.MEDIUM, "추상",
                "자유는 소중한 가치다.", null));
        words.add(createWord("정의", "올바르고 공정한 도리", Difficulty.MEDIUM, "추상",
                "정의가 실현되어야 한다.", null));
        words.add(createWord("평화", "전쟁이나 분쟁이 없는 상태", Difficulty.MEDIUM, "추상",
                "세계 평화를 기원한다.", null));
        words.add(createWord("사랑", "상대를 아끼고 소중히 여기는 마음", Difficulty.MEDIUM, "추상",
                "가족에 대한 사랑이 크다.", null));
        words.add(createWord("희망", "앞날에 대한 기대와 믿음", Difficulty.MEDIUM, "추상",
                "희망을 잃지 말아야 한다.", null));
        words.add(createWord("용기", "두려움 없이 대담하게 나아가는 마음", Difficulty.MEDIUM, "추상",
                "용기를 내서 도전했다.", null));
        words.add(createWord("인내", "괴로움이나 어려움을 참고 견딤", Difficulty.MEDIUM, "추상",
                "성공하려면 인내가 필요하다.", null));
        words.add(createWord("지혜", "사물의 이치를 깨닫고 적절히 처리하는 능력", Difficulty.MEDIUM, "추상",
                "경험을 통해 지혜를 얻었다.", null));
        words.add(createWord("성실", "거짓 없이 참되고 정성스러운 태도", Difficulty.MEDIUM, "추상",
                "성실하게 일하는 것이 중요하다.", null));
        words.add(createWord("책임", "맡은 일을 끝까지 해내야 할 의무", Difficulty.MEDIUM, "추상",
                "자신의 행동에 책임을 져야 한다.", null));

        // 관계 (10개)
        words.add(createWord("친구", "서로 친하게 지내는 사람", Difficulty.MEDIUM, "관계",
                "오랜 친구를 만났다.", null));
        words.add(createWord("가족", "함께 사는 혈연 관계의 사람들", Difficulty.MEDIUM, "관계",
                "가족과 여행을 갔다.", null));
        words.add(createWord("동료", "같은 직장에서 일하는 사람", Difficulty.MEDIUM, "관계",
                "동료와 협력하여 일했다.", null));
        words.add(createWord("스승", "학문이나 기술을 가르치는 사람", Difficulty.MEDIUM, "관계",
                "스승의 가르침을 따랐다.", null));
        words.add(createWord("이웃", "가까이 사는 사람", Difficulty.MEDIUM, "관계",
                "이웃과 인사를 나눴다.", null));
        words.add(createWord("연인", "사랑하는 사이의 두 사람", Difficulty.MEDIUM, "관계",
                "연인과 데이트를 했다.", null));
        words.add(createWord("동반자", "함께 행동하는 사람", Difficulty.MEDIUM, "관계",
                "인생의 동반자를 찾았다.", null));
        words.add(createWord("라이벌", "경쟁하는 상대", Difficulty.MEDIUM, "관계",
                "그는 나의 영원한 라이벌이다.", null));
        words.add(createWord("멘토", "조언과 지도를 해주는 사람", Difficulty.MEDIUM, "관계",
                "멘토의 조언이 큰 도움이 됐다.", null));
        words.add(createWord("협력자", "함께 일을 하는 사람", Difficulty.MEDIUM, "관계",
                "좋은 협력자를 만났다.", null));

        // 감정 심화 (10개)
        words.add(createWord("질투", "남이 잘되는 것을 시샘하는 마음", Difficulty.MEDIUM, "감정",
                "친구의 성공에 질투를 느꼈다.", null));
        words.add(createWord("외로움", "혼자 있어 쓸쓸한 감정", Difficulty.MEDIUM, "감정",
                "혼자 있을 때 외로움을 느낀다.", null));
        words.add(createWord("불안", "마음이 편하지 않고 조마조마한 상태", Difficulty.MEDIUM, "감정",
                "시험 전에 불안감이 컸다.", null));
        words.add(createWord("후회", "이미 한 일에 대해 아쉬워하는 마음", Difficulty.MEDIUM, "감정",
                "그 선택을 후회하고 있다.", null));
        words.add(createWord("감동", "마음이 크게 움직이는 느낌", Difficulty.MEDIUM, "감정",
                "영화를 보고 감동받았다.", null));
        words.add(createWord("당황", "갑작스러운 일에 어찌할 바를 모르는 상태", Difficulty.MEDIUM, "감정",
                "예상치 못한 질문에 당황했다.", null));
        words.add(createWord("안도", "걱정이 사라지고 마음이 편해지는 느낌", Difficulty.MEDIUM, "감정",
                "무사히 끝나서 안도했다.", null));
        words.add(createWord("감사", "고마운 마음", Difficulty.MEDIUM, "감정",
                "도움에 깊은 감사를 느낀다.", null));
        words.add(createWord("존경", "높이 우러러 받드는 마음", Difficulty.MEDIUM, "감정",
                "그의 노력을 존경한다.", null));
        words.add(createWord("설렘", "기대감에 가슴이 두근거리는 느낌", Difficulty.MEDIUM, "감정",
                "내일 여행에 대한 설렘이 크다.", null));

        return words;
    }

    private List<WordPool> createHardWords() {
        List<WordPool> words = new ArrayList<>();

        // 심리/철학 (7개)
        words.add(createWord("배신", "믿음을 저버리고 등을 돌리는 행위", Difficulty.HARD, "추상",
                "친구의 배신에 상처받았다.", null));
        words.add(createWord("역설", "겉으로는 모순되지만 실제로는 진실인 명제", Difficulty.HARD, "추상",
                "이 상황은 하나의 역설이다.", null));
        words.add(createWord("딜레마", "어느 쪽을 선택해도 불리한 상황", Difficulty.HARD, "추상",
                "윤리적 딜레마에 빠졌다.", null));
        words.add(createWord("아이러니", "반어법, 겉과 속이 다른 상황", Difficulty.HARD, "추상",
                "이것은 아이러니한 상황이다.", null));
        words.add(createWord("카타르시스", "감정의 정화와 해소", Difficulty.HARD, "추상",
                "영화를 보며 카타르시스를 느꼈다.", null));
        words.add(createWord("허무", "모든 것이 덧없고 무의미하게 느껴지는 상태", Difficulty.HARD, "감정",
                "성공 후 허무함을 느꼈다.", null));
        words.add(createWord("향수", "지나간 일을 그리워하는 마음", Difficulty.HARD, "감정",
                "고향에 대한 향수를 느낀다.", null));

        // 게임/전략 심화 (7개)
        words.add(createWord("블러핑", "거짓으로 상대를 속이는 전략", Difficulty.HARD, "게임",
                "포커에서 블러핑이 통했다.", null));
        words.add(createWord("견제", "상대의 행동을 막거나 제한하는 것", Difficulty.HARD, "게임",
                "상대팀을 견제하는 전략을 썼다.", null));
        words.add(createWord("역전", "불리한 상황을 유리하게 바꾸는 것", Difficulty.HARD, "게임",
                "마지막에 극적인 역전승을 거뒀다.", null));
        words.add(createWord("심리전", "상대의 마음을 읽고 흔드는 싸움", Difficulty.HARD, "게임",
                "심리전에서 우위를 점했다.", null));
        words.add(createWord("연합", "공동의 목적을 위해 힘을 합침", Difficulty.HARD, "관계",
                "강한 적에 맞서 연합을 결성했다.", null));
        words.add(createWord("은신", "숨어서 모습을 드러내지 않는 것", Difficulty.HARD, "행동",
                "적의 눈을 피해 은신했다.", null));
        words.add(createWord("함정", "상대를 속여 빠뜨리는 장치나 계략", Difficulty.HARD, "추상",
                "교묘한 함정에 빠졌다.", null));

        // 복잡한 관계/사회 (6개)
        words.add(createWord("공모", "남몰래 같은 목적을 위해 함께 꾀함", Difficulty.HARD, "관계",
                "범죄에 공모한 혐의를 받았다.", null));
        words.add(createWord("갈등", "서로 다른 의견이나 이해관계가 부딪침", Difficulty.HARD, "관계",
                "팀 내부에 갈등이 생겼다.", null));
        words.add(createWord("타협", "서로 양보하여 합의점을 찾는 것", Difficulty.HARD, "관계",
                "의견 차이를 타협으로 해결했다.", null));
        words.add(createWord("중재", "분쟁 중인 양측 사이에서 조정하는 것", Difficulty.HARD, "관계",
                "제3자가 중재에 나섰다.", null));
        words.add(createWord("동맹", "공동의 이익을 위한 정치적 결합", Difficulty.HARD, "관계",
                "두 나라가 동맹을 맺었다.", null));
        words.add(createWord("반목", "서로 미워하고 다투는 사이", Difficulty.HARD, "관계",
                "오랜 반목 끝에 화해했다.", null));

        return words;
    }

    /**
     * WordPool 생성 헬퍼 메서드
     */
    private WordPool createWord(String word, String description, Difficulty difficulty,
                                String category, String exampleSentence, String synonyms) {
        return WordPool.builder()
                .word(word)
                .description(description)
                .difficulty(difficulty)
                .category(category)
                .exampleSentence(exampleSentence)
                .synonyms(synonyms)
                .build();
    }

    /**
     * 난이도별 개수 조회 (로깅용)
     */
    private long countByDifficulty(Difficulty difficulty) {
        return wordPoolRepository.findByDifficultyAndIsActiveTrue(difficulty).size();
    }
}