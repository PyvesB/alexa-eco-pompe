package io.github.pyvesb.alexaecopompe.handlers;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.builder.CustomSkillBuilder;

/**
 * Class used as a handler for AWS Lambda function calls. Delegates all processing to one of the request handler
 * instances. The handler field in the AWS Lambda console needs to be set to the following:
 * io.github.pyvesb.alexaecopompe.handlers.EcoPompeStreamHandler
 * 
 * @author Pyves
 *
 */
public class EcoPompeStreamHandler extends SkillStreamHandler {

	private static final String SKILL_ID = "amzn1.ask.skill.1018a411-9332-46b3-b573-31ae7e591ebb";

	public EcoPompeStreamHandler() {
		super(getSkill());
	}

	private static Skill getSkill() {
		return new CustomSkillBuilder()
				.addRequestHandlers(new CancelStopIntentHandler(),
						new HelpIntentHandler(),
						new MainIntentHandler(), // Must be specified as the last intent handler.
						new SessionEndedRequestHandler(),
						new LaunchRequestHandler())
				.withSkillId(SKILL_ID)
				.build();
	}

}
